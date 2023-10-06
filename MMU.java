import java.util.*;
import java.io.File;

class MMU {
    private int memoria;
    private String politica;
    private List<Partition> partitions;
    private Map<String, Partition> allocatedPartitions;

    public MMU(int memoria, String politica) {
        this.memoria = memoria;
        this.politica = politica;
        this.partitions = new ArrayList<>();
        this.allocatedPartitions = new HashMap<>();
        Partition initialPartition = new Partition(0, memoria, false);
        this.partitions.add(initialPartition);
    }

    public void processFile(File f) {
        try{
        Scanner scanner = new Scanner(f);
        System.out.println("\n | " + memoria + " |\n");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                String[] tokens = line.split("\\(");

                if (tokens.length != 2) {
                    System.out.println("LINHA INVALIDA: '" + line + "'");
                    return;
                }

                String command = tokens[0].trim();
                String arguments = tokens[1].replaceAll("\\)", "").trim();

                switch (command) {
                    case "IN":
                        String[] inArgs = arguments.split(",");
                        if (inArgs.length != 2) {
                            System.out.println("REQUISICAO INVALIDA: '" + line + "'");
                            return;
                        }
                        String processId = inArgs[0].trim();
                        int size = Integer.parseInt(inArgs[1].trim());
                        allocateProcess(processId, size);
                        printPartitions();
                        break;

                    case "OUT":
                        String processToRelease = arguments.trim();
                        releaseProcess(processToRelease);
                        printPartitions();
                        break;

                    default:
                        System.out.println("COMANDO INVALIDO: '" + command + "' \n");
                }
            }
        }
		scanner.close();
    }catch(Exception e){
        e.printStackTrace();
    }
    }

    public void allocateProcess(String processId, int size) {
        boolean allocated = false;

        if (politica.contains("worst")) {
            allocated = allocateWorstFit(processId, size);
        }
          else if (politica.contains("circular")) {
            allocated = allocateCircularFit(processId, size);
        }

        if (allocated) {
            System.out.println("IN(" + processId + ", " + size + ") - Processo alocado.");
        } else {
            System.out.println("IN(" + processId + ", " + size + ") - ERRO - ESPACO INSUFICIENTE DE MEMORIA");
        }
    }

    private boolean allocateWorstFit(String processId, int size) {
        int largestPartitionIndex = -1;
        int largestPartitionSize = -1;

        for (int i = 0; i < partitions.size(); i++) {
            Partition partition = partitions.get(i);

            if (!partition.isAllocated() && partition.getSize() >= size) {
                if (partition.getSize() > largestPartitionSize) {
                    largestPartitionSize = partition.getSize();
                    largestPartitionIndex = i;
                }
            }
        }

        if (largestPartitionIndex != -1) {
            Partition partition = partitions.get(largestPartitionIndex);

            if (partition.getSize() == size) {
                partition.setAllocated(true);
            } else {
                Partition newPartition = new Partition(partition.getStartAddress() + size, partition.getSize() - size,
                        false);
                partition.setSize(size);
                partition.setAllocated(true);
                partitions.add(largestPartitionIndex + 1, newPartition);
            }

            allocatedPartitions.put(processId, partition);
            return true;
        }

        return false;
    }

    private boolean allocateCircularFit(String processId, int size) {
        int startIndex = -1;

        // Acha primeira particao disponivel em que caiba o processo
        for (int i = 0; i < partitions.size(); i++) {
            Partition partition = partitions.get(i);

            if (!partition.isAllocated() && partition.getSize() >= size) {
                startIndex = i;
                break;
            }
        }

        if (startIndex != -1) {
            // Procura circularmente pela particao de maior tamanho
            int currentIndex = startIndex;
            int largestPartitionIndex = -1;
            int largestPartitionSize = 0;

            do {
                Partition partition = partitions.get(currentIndex);

                if (!partition.isAllocated() && partition.getSize() >= size
                        && (largestPartitionIndex == -1 || partition.getSize() > largestPartitionSize)) {
                    largestPartitionSize = partition.getSize();
                    largestPartitionIndex = currentIndex;
                }

                currentIndex = (currentIndex + 1) % partitions.size();
            } while (currentIndex != startIndex);

            // Aloca o processo na maior particao disponivel
            Partition largestPartition = partitions.get(largestPartitionIndex);

            if (largestPartition.getSize() == size) {
                largestPartition.setAllocated(true);
            } else {
                Partition newPartition = new Partition(largestPartition.getStartAddress() + size,
                        largestPartition.getSize() - size, false);
                largestPartition.setSize(size);
                largestPartition.setAllocated(true);
                partitions.add(largestPartitionIndex + 1, newPartition);
            }

            allocatedPartitions.put(processId, largestPartition);
            return true;
        }

        return false;
    }

    public void releaseProcess(String processId) {
        if (allocatedPartitions.containsKey(processId)) {
            Partition partition = allocatedPartitions.get(processId);
            partition.setAllocated(false);
            allocatedPartitions.remove(processId);
            //if (politica.contains("worst"))
                mergeAdjacentFreePartitions();
            System.out.println("OUT(" + processId + ") - Espaco liberado.");
        } else {
            System.out.println("OUT(" + processId + ") - Processo nao encontrado.");
        }
    }

    private void mergeAdjacentFreePartitions() {
        Collections.sort(partitions, Comparator.comparing(Partition::getStartAddress));

        for (int i = 0; i < partitions.size() - 1; i++) {
            Partition currentPartition = partitions.get(i);
            Partition nextPartition = partitions.get(i + 1);

            if (!currentPartition.isAllocated() && !nextPartition.isAllocated()) {
                currentPartition.setSize(currentPartition.getSize() + nextPartition.getSize());
                partitions.remove(nextPartition);
                i--; // Verifica a posicao atual apos juntar
            }
        }
    }

    public void printPartitions() {
        System.out.println();
        for (Partition partition : partitions) {
            if (!partition.isAllocated()) {
                System.out.print(" | " + partition.getSize());
            }
        }
        System.out.println(" | \n ___________________________ \n");
    }

}

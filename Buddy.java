import java.io.File;
import java.util.*;

class Buddy {
    public int memoria;
    public int totalFragmentation = 0;
    private List<Partition> partitions;
    private Map<String, Partition> allocatedPartitions;

    public Buddy(int memoria) {
        this.partitions = new ArrayList<>();
        this.allocatedPartitions = new HashMap<>();
        Partition initialPartition = new Partition(0, memoria, false, 0);
        this.partitions.add(initialPartition);
    }

    public void processFile(File f) {
        try{
        Scanner scanner = new Scanner(f);
        printPartitions();
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
                        allocateBuddy(processId, size);
                        printPartitions();
                        break;

                    case "OUT":
                        String processToRelease = arguments.trim();
                        deallocate(processToRelease);
                        printPartitions();
                        break;

                    default:
                        System.out.println("COMANDO INVALIDO: '" + command + "' \n");
                }
            }
        }
		scanner.close();
        defrag();
    } catch(Exception e){
        e.printStackTrace();
    }
    }

    public boolean allocateBuddy(String processId, int size) {
        int blockSize = getNextPowerOfTwo(size);

        for (Partition partition : partitions) {
            if (!partition.isAllocated() && partition.getSize() >= blockSize) {
                if (partition.getSize() != blockSize) {
                    partition = splitPartitions(partition, blockSize);
                }
                partition.setAllocated(true);
                partition.setFragmentation(blockSize - size);
                allocatedPartitions.put(processId, partition);
                System.out.println("IN(" + processId + ", " + size + ") - Processo alocado.");
                totalFragmentation += (blockSize - size);
                return true;
            }
        }
        System.out.println("ESPAÇO INSUFICIENTE PARA "+processId);
        return false;
    }

    // Recebe a particao a ser alocada e o tamanho do processo a que vai alocar, vai dividindo as particoes
    public Partition splitPartitions(Partition startingPartition, int size){
        int splitSize = startingPartition.getSize() / 2;

        startingPartition.setSize(splitSize);
        Partition newPartition = new Partition(startingPartition.getStartAddress() + splitSize, splitSize, false, 0);
        partitions.add(partitions.indexOf(startingPartition) + 1, newPartition);
        
        if(splitSize > size)
            return splitPartitions(startingPartition, size);

        return startingPartition;
    }

    public void deallocate(String processId) {
        if (allocatedPartitions.containsKey(processId)) {
            Partition partition = allocatedPartitions.get(processId);
            partition.setAllocated(false);
            totalFragmentation -= partition.getFragmentation();
            partition.setFragmentation(0);
            allocatedPartitions.remove(processId);
            System.out.println("OUT(" + processId + ") - Espaço liberado.");
            coalesce();
        }
        else{System.out.println("PROCESSO "+processId+ " NAO ALOCADO");}
    }

    private void coalesce() { // Junta todas as particoes adjacentes (termo do livro)
        Collections.sort(partitions, Comparator.comparing(Partition::getStartAddress));

        for (int i = 0; i < partitions.size() - 1; i++) {
            Partition currentPartition = partitions.get(i);
            Partition nextPartition = partitions.get(i + 1);

            if (!currentPartition.isAllocated() && !nextPartition.isAllocated()
                    && currentPartition.getSize() == nextPartition.getSize()) {
                currentPartition.setSize(currentPartition.getSize() * 2);
                partitions.remove(nextPartition);
                i--;
            }
        }
    }

    private int getNextPowerOfTwo(int size) {
        int power = 1;
        while (power < size) {
            power *= 2;
        }
        return power;
    }


    public void printPartitions() {
        System.out.println();
        Collections.sort(partitions, Comparator.comparing(Partition::getStartAddress));
        for (Partition partition : partitions) {
            if(!partition.isAllocated())
                System.out.print(" | " + partition.getSize());  
        }
        System.out.println(" | ");
        System.out.println("\n Fragmentacao interna total: "+totalFragmentation);
        System.out.println("\n ___________________________ \n");
    }

    public void defrag(){
        if(partitions.size()>1){
            System.out.println("Desfragmentando memória...");
        
        while(partitions.size()>1){
            coalesce();
        }
        printPartitions();
    }
    }
}

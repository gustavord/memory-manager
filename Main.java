import java.util.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
            File settings = new File("settings.txt");
            if(settings.exists())
                System.out.println("settings.txt lido com sucesso!");
            
            else{System.out.println("Arquivo nao encontrado. Encerrando..."); System.exit(1);}

            Scanner teclado = new Scanner(System.in);

            System.out.println("Digite o tamanho inicial da memoria:");

            int memoria = 1;
            do{
                memoria = teclado.nextInt();
                System.out.println("Atencao: O tamanho deve ser equivalente a uma potencia de dois!");
            } while(!isPowerOfTwo(memoria));

            System.out.println("Digite a politica a ser empregada (worst-fit | circular-fit | buddy)");

            String politica = teclado.next();
             if(politica.contains("buddy")){
                Buddy buddy = new Buddy(memoria);
                System.out.println("Politica: "+politica+"\n Memoria principal: "+memoria);
                buddy.processFile(settings);
            }
            else if(politica.contains("worst") || politica.contains("circular")){
                MMU MMU = new MMU(memoria, politica);
                System.out.println("Politica: "+politica+"\n Memoria principal: "+memoria);
                MMU.processFile(settings);
            }
            else{
                System.out.println("Selecao invalida.");
                System.exit(1);
            }
            teclado.close();
    }
    private static boolean isPowerOfTwo(int number) {
        return (number & (number - 1)) == 0;
    }
}
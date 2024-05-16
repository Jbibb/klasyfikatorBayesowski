import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class Main {

    private static final String TRAINING_PATH = "files/iris_training.txt";
    private static final String TEST_PATH = "files/iris_test.txt";
    private static final int INITIAL_THRESHOLD = 0;

    private static ArrayList<Observation> trainingSet = new ArrayList<>();
    private static int attributeAmount;
    private static int trainingIterationCount = 0;

    public static void main(String[] args) {

        try (BufferedReader br = new BufferedReader(new FileReader(TRAINING_PATH))) {

            while (br.ready()) {
                trainingSet.add(createObservation(br.readLine()));
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        attributeAmount = trainingSet.get(0).getAttributes().length;
        Observation.setEndOfTrainingFlag(true);

        boolean exit = false;
        Scanner sc = new Scanner(System.in);
        String in;
        String[] splitLine;
        double[] customAttributes;
        while (!exit) {
            System.out.println("'t' - test\t'c' - classify custom (seperate attributes with space)\t'e' - exit");
            in = sc.nextLine();
            if(in.equals("t")){
                runTest();
            } else if(in.equals("c")){
                System.out.print(Observation.nonDecisionAttributeAmount + " attributes: ");
                splitLine = sc.nextLine().split(" ");
                customAttributes = new double[Observation.nonDecisionAttributeAmount];
                try {
                    if(splitLine.length != Observation.nonDecisionAttributeAmount){
                        throw new NumberFormatException();
                    }
                    for (int i = 0; i < splitLine.length; i++) {
                        customAttributes[i] = Double.parseDouble(splitLine[i]);
                    }
                    System.out.println(classify(new Observation(customAttributes, null, null)));
                } catch (NumberFormatException e){
                    System.out.println("bad format");
                }

            } else {
                exit = true;
            }

        }

    }

    private static void runTest(){
        int[][] mistakeMatrix = new int[Observation.possibleDecisionAttributes.size()][Observation.possibleDecisionAttributes.size()];
        Map<String, Integer> indexesForDecisionAttributes = new HashMap<>();
        int index = 0;
        for(String decisionAttribute : Observation.possibleDecisionAttributes){
            indexesForDecisionAttributes.put(decisionAttribute, index++);
        }

        Observation observation;
        String classificationResult;
        int total = 0, correctClassifications = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(TEST_PATH))) {

            while (br.ready()) {
                observation = createObservation(br.readLine());
                classificationResult = classify(observation);
                total++;
                if(classificationResult.equals(observation.getExpectedDecisionAttribute()))
                    correctClassifications++;
                System.out.println("#" + total + " W rzeczywistości: " + observation.getExpectedDecisionAttribute() + ", Zaklasyfikowano: " + classificationResult);
                mistakeMatrix[indexesForDecisionAttributes.get(observation.getExpectedDecisionAttribute())][indexesForDecisionAttributes.get(classificationResult)]++;
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("\t\t\t==========WYNIKI TESTU==========\t\t\t\n");
        System.out.println("\tPROCENT POPRAWNYCH KLASYFIKACJI: " + (double) (Math.round((double) correctClassifications/total * 10000)) / 100 + "%\n");
        System.out.println("\t\t\t\t\t\t\t\t\\/ ZAKLASYFIKOWANO JAKO \\/");
        System.out.print("\tW RZECZYWISTOŚCI");
        for (String possibleDecisionAttribute : Observation.possibleDecisionAttributes)
            System.out.print("\t" + possibleDecisionAttribute + "\t");
        System.out.println();
        for (int i = 0; i < mistakeMatrix.length; i++) {
            System.out.print("\t" + Observation.possibleDecisionAttributes.toArray()[i] + "\t\t\t\t\t");
            for (int j = 0; j < mistakeMatrix[0].length; j++)
                System.out.print(mistakeMatrix[i][j] + "\t\t\t\t");
            System.out.print("\n\n");
        }
        double[] precisions = new double[Observation.possibleDecisionAttributes.size()];
        double[] recalls = new double[Observation.possibleDecisionAttributes.size()];

        System.out.println("\tPRECYZJE:");
        for(int i = 0; i < mistakeMatrix.length; i++){
            total = 0;
            correctClassifications = 0;
            for (int j = 0; j < mistakeMatrix[0].length; j++){
                total += mistakeMatrix[j][i];
                if(i == j){
                    correctClassifications += mistakeMatrix[i][j];
                }
            }
            System.out.println("\t\t - " + Observation.possibleDecisionAttributes.toArray()[i] + ": " + correctClassifications + "/" + total);
            precisions[i] = (double) correctClassifications/total;
        }

        System.out.println("\n\tPEŁNOŚCI:");
        for(int i = 0; i < mistakeMatrix.length; i++){
            total = 0;
            correctClassifications = 0;
            for (int j = 0; j < mistakeMatrix[0].length; j++){
                total += mistakeMatrix[i][j];
                if(i == j){
                    correctClassifications += mistakeMatrix[i][j];
                }
            }
            System.out.println("\t\t - " + Observation.possibleDecisionAttributes.toArray()[i] + ": " + correctClassifications + "/" + total);
            recalls[i] = (double) correctClassifications / total;
        }

        System.out.println("\n\tF-MIARY:");
        for(int i = 0; i < Observation.possibleDecisionAttributes.size(); i++) {
            System.out.println("\t\t - " + Observation.possibleDecisionAttributes.toArray()[i] + ": " + (Math.round(((2 * precisions[i] * recalls[i]) / (precisions[i] + recalls[i])) * 1000)) / 1000d );
        }
    }

    private static String classify(Observation classifiedObservation) {
        double[] probabilities = new double[Observation.possibleDecisionAttributes.size()];
        Arrays.fill(probabilities, 1);

        int index = 0;
        double probability;
        int count;

        for(String decisionAttribute : Observation.possibleDecisionAttributes) {
            for(int i = 0; i < classifiedObservation.getAttributes().length; i++) {
                count = 0;
                for(Observation observation : trainingSet){
                    if(decisionAttribute.equals(observation.getDecisionAttribute()) && observation.getAttributes()[i] == classifiedObservation.getAttributes()[i])
                        count++;
                }
                if(count == 0) {
                    System.out.print("brak obserwacji z atrybutem (kol " + i + ") = " + classifiedObservation.getAttributes()[i] + " dla atrybutu decyzyjnego = " + decisionAttribute + " - wygładzanie ");
                    probability = (double) (count + 1) / (trainingSet.size() + Observation.possibleAttributes[i].size());
                    System.out.println("przed: " + count + "/" + trainingSet.size() + " po: " + (count + 1) + "/" + (trainingSet.size() + Observation.possibleAttributes[i].size()));
                } else {
                    probability = (double) count / trainingSet.size();
                }
                probabilities[index] *= probability;
            }
            count = 0;
            for(Observation observation : trainingSet)
                if(observation.getDecisionAttribute().equals(decisionAttribute))
                    count++;
            /*if(count == 0) {
                System.out.print("wygładzanie ");
                probability = (double) (count + 1) / (trainingSet.size() + Observation.possibleDecisionAttributes.size());
                System.out.println("przed: " + count + "/" + trainingSet.size() + " po: " + (count + 1) + "/" + (trainingSet.size() + Observation.possibleDecisionAttributes.size()));
            } else {
                probability = (double) count / trainingSet.size();
            }*/
            probability = (double) count / trainingSet.size();

            probabilities[index++] *= probability;
        }
        double max = 0;
        index = 0;
        for(int i = 0; i < probabilities.length; i++){
            if(probabilities[i] > max) {
                max = probabilities[i];
                index = i;
            }
        }

        return (String) Observation.possibleDecisionAttributes.toArray()[index];
    }

    private static Observation createObservation(String fileLine) {
        String[] lineData;
        double[] attributes;

        lineData = fileLine.trim().split("[\s\t]+");

        attributes = new double[lineData.length - 1];
        for(int i = 0; i < lineData.length - 1; i++)
            attributes[i] = Double.parseDouble(lineData[i].replace(',', '.'));

        return new Observation(attributes, lineData[lineData.length - 1], lineData[lineData.length - 1]);
    }
}
import java.util.*;

public class Observation {
    public static Integer nonDecisionAttributeAmount;
    public static Set<String> possibleDecisionAttributes = new LinkedHashSet<>();
    public static Set<Double>[] possibleAttributes;
    public static boolean endOfTrainingFlag = false;
    private final double[] attributes;
    private final String decisionAttribute;
    private String expectedDecisionAttribute;

    public Observation(double[] attributes, String decisionAttribute, String expectedDecisionAttribute) {
        this.expectedDecisionAttribute = expectedDecisionAttribute;

        if(nonDecisionAttributeAmount == null) {
            nonDecisionAttributeAmount = attributes.length;
            possibleAttributes = new HashSet[nonDecisionAttributeAmount];
            for(int i = 0; i < possibleAttributes.length; i++)
                possibleAttributes[i] = new HashSet<>();
        } else if (nonDecisionAttributeAmount != attributes.length)
            throw new ArrayIndexOutOfBoundsException();
        if(!endOfTrainingFlag) {
            for (int i = 0; i < nonDecisionAttributeAmount; i++) {
                possibleAttributes[i].add(attributes[i]);
            }
            possibleDecisionAttributes.add(decisionAttribute);
        }

        this.attributes = attributes;
        this.decisionAttribute = decisionAttribute;

    }

    public static void setEndOfTrainingFlag(boolean val){
        endOfTrainingFlag = val;
    }

    public double[] getAttributes() {
        return attributes;
    }

    public String getDecisionAttribute() {
        return decisionAttribute;
    }

    public String getExpectedDecisionAttribute() {
        return expectedDecisionAttribute;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(double attribute : attributes)
            sb.append(attribute).append("    ");
        sb.append(decisionAttribute);
        return sb.toString();
    }
}
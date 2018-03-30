package rocketsolrapp.clientapi.schema;

public class Field {

    private final String name;
    private final float weight;
    private final DocType docType;
    private final FieldType fieldType;

    public Field(String name, float weight, DocType docType, FieldType fieldType) {
        this.name = name;
        this.weight = weight;
        this.docType = docType;
        this.fieldType = fieldType;
    }

    public String buildQueryPartWithKeyword(String keyword){
        return name + ":\"" + keyword + "\"^" + weight;
    }

    public String getName() {
        return name;
    }

    public float getWeight() {
        return weight;
    }

    public DocType getDocType() {
        return docType;
    }

    public FieldType getFieldType() {
        return fieldType;
    }
}

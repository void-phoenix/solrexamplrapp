package rocketsolrapp.clientapi.schema;


import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductQueryBuilder {

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String COLOR = "color";
    private static final String SIZE = "size";

    private List<Field> fields;


    @PostConstruct
    private void init(){
        fields = new ArrayList<>();
        fields.add(new Field(TITLE, 2.0f, DocType.PRODUCT, FieldType.TEXT));
        fields.add(new Field(DESCRIPTION, 1.0f, DocType.PRODUCT, FieldType.TEXT));
        fields.add(new Field(COLOR, 1.0f, DocType.SKU, FieldType.TEXT));
        fields.add(new Field(SIZE, 1.0f, DocType.SKU, FieldType.TEXT));
    }

    public String buildProductTextQuery(String keywords){
        StringBuilder builder = new StringBuilder();

        String productQuery = getProductTextFields().stream().map(field -> {
            String requestPart = field.buildQueryPartWithKeyword(keywords);
            return requestPart;
        }).collect(Collectors.joining(" "));
        builder.append(productQuery);
        builder.append(" ");

        builder.append("+_query_:\"{!parent which=docType:product}\" ");

        String skusQuery = getSkuTextFields().stream().map(field -> {
            String requestPart = field.buildQueryPartWithKeyword(keywords);
            return requestPart;
        }).collect(Collectors.joining(" "));

        builder.append(skusQuery);

        return builder.toString();

    }


    public String buildProducsWithSkuTextQuery(String keywords){

        return getProductTextFields().stream().map(field -> {
            String requestPart = field.buildQueryPartWithKeyword(keywords);
            return requestPart;
        }).collect(Collectors.joining(" "));
    }

    private List<Field> getProductTextFields(){
        return fields.stream().filter(f -> f.getDocType().equals(DocType.PRODUCT) &&
                (f.getName().equals(TITLE) || f.getName().equals(DESCRIPTION)))
                .collect(Collectors.toList());
    }

    private List<Field> getSkuTextFields(){
        return fields.stream().filter(f -> f.getDocType().equals(DocType.SKU) &&
                (f.getName().equals(TITLE) || f.getName().equals(DESCRIPTION)))
                .collect(Collectors.toList());
    }

}

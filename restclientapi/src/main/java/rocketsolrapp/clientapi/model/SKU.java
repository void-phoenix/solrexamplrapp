package rocketsolrapp.clientapi.model;

import javax.validation.constraints.NotNull;

public class SKU {

    @NotNull
    private String id;
    private String color;
    private String size;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

}

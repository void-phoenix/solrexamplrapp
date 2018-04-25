package rocketsolrapp.clientapi.model.product;

import javax.validation.constraints.NotNull;

public class SKU {

    @NotNull
    private String id;
    private String color;
    private String size;
    private int store_0;

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

    public int isStore_0() {
        return store_0;
    }

    public void setStore_0(int store_0) {
        this.store_0 = store_0;
    }
}

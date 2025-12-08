package com.example.thinglistbeta;

public class ThingItem {

    public String name;
    public String description;
    public String price;     // keep as String because EditItem uses a text field
    public String location;
    public String status;
    public String imagePath; // local file path for the photo (can be null)

    public ThingItem(String name,
                     String description,
                     String price,
                     String location,
                     String status,
                     String imagePath) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.location = location;
        this.status = status;
        this.imagePath = imagePath;
    }
}

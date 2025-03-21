package com.example.fitlab;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Exercise {
    @SerializedName("bodyPart")
    private String bodyPart;

    @SerializedName("equipment")
    private String equipment;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("target")
    private String target;

    @SerializedName("secondaryMuscles")
    private List<String> secondaryMuscles;

    @SerializedName("instructions")
    private List<String> instructions;

    // Getters and setters
    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(List<String> secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    @Override
    public String toString() {
        return name;
    }
}
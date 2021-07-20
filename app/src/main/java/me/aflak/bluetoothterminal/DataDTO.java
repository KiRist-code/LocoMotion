package me.aflak.bluetoothterminal;

import java.util.ArrayList;

public class DataDTO {
    private ArrayList<Float> uX;
    private ArrayList<Float> dX;

    public DataDTO() { }

    public DataDTO(ArrayList<Float> uX, ArrayList<Float> dX) {
        this.uX = uX;
        this.dX = dX;
    }

    public ArrayList<Float> getUX() {
        return uX;
    }

    public void setUX(ArrayList<Float> uX) {
        this.uX = uX;
    }

    public ArrayList<Float> getDX() {
        return dX;
    }

    public void setDX(ArrayList<Float> dX) {
        this.dX = dX;
    }
}

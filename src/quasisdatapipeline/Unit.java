/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quasisdatapipeline;

/**
 *
 * @author Wei Wang
 */
public class Unit {

    private String julienBarcode, pillarId, row;
    private int col;
    private float[] unitArr;

    public Unit(String julienBarcode, String pillarId, String row, int col, float[] unitArr) {
        this.julienBarcode = julienBarcode;
        this.unitArr = unitArr;
        this.pillarId = pillarId;
        this.row = row;
        this.col = col;
    }

    public Unit(String julienBarcode) {
        this.julienBarcode = "Dup" + julienBarcode;
        this.pillarId = "Dup";
        this.row = "Dup";
        this.col = -1;
    }

    public void setUnitArr(float[] unitArr) {
        this.unitArr = unitArr;
    }

    public void setUnitValue(int index, float val) throws Exception {
        if (index < 0 || index >= this.unitArr.length) {
            throw new Exception("Array out of Bound Exception" + index + " vs  " + this.unitArr.length);
        }
        this.unitArr[index] = val;
    }

    public String getJulien() {
        return this.julienBarcode;
    }

    public String getPillarId() {
        return this.pillarId;
    }

    public String getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public float[] getUnitArr() {
        return this.unitArr;
    }

}

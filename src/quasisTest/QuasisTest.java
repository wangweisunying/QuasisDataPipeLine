/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quasisTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Wei Wang
 */
public class QuasisTest {
    protected String dataTable , testType , negativeLocation , testName , unitDataTable;
    protected String[] indexTestMapArr , indexTestTitleArr;
    protected float[][] indexTestEquationOffset;
    protected float[][] indexTestUnitThreshold;
    protected float[] YStandard;
    protected Map<String , Integer> caliIndexMap;
    protected Map<Integer , List<Integer>> oldPanelIndex2NewPanelIndexMap;
    QuasisTest(){
        this.caliIndexMap = new HashMap();
    }
    public String getUnitDataTable(){
        return this.unitDataTable;
    }
    public String getTestName(){
        return this.testName;
    }
    public Map<Integer , List<Integer>>  getOldPanelIndex2NewPanelIndex(){
        return this.oldPanelIndex2NewPanelIndexMap;
    }
    
    public String getTestType(){
        return this.testType;
    }
    public String[] getTestMapArr(){
        return this.indexTestMapArr;
    }
    
    public String[] getIndexTestTitleArr(){
        return this.indexTestTitleArr;
    }
    
    public float[] getYStandard(){
        return this.YStandard;
    }
    public Map<String , Integer>getCaliIndexMap(){
        return this.caliIndexMap;
    }
    public String getDataTable(){
        return this.dataTable;
    }
    public String getNegativeLocation(){
        return this.negativeLocation;
    }
    public float[][] getIndexTestUnitThreshold(){
        return this.indexTestUnitThreshold;
    }
    public float[][] getIndexTestEquationOffset(){
        return this.indexTestEquationOffset;
    }
}

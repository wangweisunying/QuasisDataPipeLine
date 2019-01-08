/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quasisTest;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Wei Wang
 */
public class FoodAllergy12 extends QuasisTest{
    
    
    public FoodAllergy12(){
        super();
        this.oldPanelIndex2NewPanelIndexMap = new HashMap();
        this.oldPanelIndex2NewPanelIndexMap.computeIfAbsent( 3 , x -> new ArrayList()).add(8);
        this.oldPanelIndex2NewPanelIndexMap.computeIfAbsent( 4 , x -> new ArrayList()).add(13);
        this.oldPanelIndex2NewPanelIndexMap.computeIfAbsent( 5 , x -> new ArrayList()).add(11);
        
        this.oldPanelIndex2NewPanelIndexMap.computeIfAbsent( 7 , x -> new ArrayList()).add(4);
        this.oldPanelIndex2NewPanelIndexMap.computeIfAbsent( 11 , x -> new ArrayList()).add(9);
        this.oldPanelIndex2NewPanelIndexMap.computeIfAbsent( 11 , x -> new ArrayList()).add(10);
        this.oldPanelIndex2NewPanelIndexMap.computeIfAbsent( 13 , x -> new ArrayList()).add(6);
        this.oldPanelIndex2NewPanelIndexMap.computeIfAbsent( 14 , x -> new ArrayList()).add(7);
        this.oldPanelIndex2NewPanelIndexMap.computeIfAbsent( 16 , x -> new ArrayList()).add(1);
        
        this.testName = "FAAE_12";
        this.testType  = "Food_Allergy_12";
        this.dataTable = "`vibrant_test_raw_data`.`food_allergy_12`";
        this.unitDataTable = "tsp_test_unit_data.faae_unit_data";
        this.negativeLocation = "H12";
        this.indexTestTitleArr = new String[]{
                                            "TUNA_IGE",
                                            "OYSTER_IGE",
                                            "SOYBEAN_IGE",
                                            "CRAB_IGE",
                                            "ENGLISH_WALNU_IGE",
                                            "SHRIMP_IGE",
                                            "COWS_MILK_IGE",
                                            "EGG_WHITE_IGE",
                                            "EGG_YOLK_IGE",
                                            "CORN_IGE",
                                            "WAIGE",
                                            "PEANUT_IGE",
                                            };
        
        this.indexTestMapArr = new String[]{"Positive control",
                                            "TUNA_IGE",
                                            "OYSTER_IGE",
                                            "Positive control",
                                            "SOYBEAN_IGE",
                                            "CRAB_IGE",
                                            "ENGLISH_WALNU_IGE",
                                            "SHRIMP_IGE",
                                            "COWS_MILK_IGE",
                                            "EGG_WHITE_IGE",
                                            "EGG_YOLK_IGE",
                                            "CORN_IGE",
                                            "Positive control",
                                            "WHEAT_IGE",
                                            "PEANUT_IGE",
                                            "Positive control"
                                            };
        
        
        this.indexTestEquationOffset = new float[][]{{-1 , -1},
                                               {0 , 0},
                                               {0 , 0},
                                               {-1 , -1},
                                               {0 , 0},
                                               {0 , 0},
                                               {0 , 0},
                                               {0 , 0},
                                               {0 , 0},
                                               {0 , 0},
                                               {0 , 0},
                                               {0 , 0},
                                               {-1 , -1},
                                               {0 , 0},
                                               {0 , 0},
                                               {-1 , -1}};
        
        this.indexTestUnitThreshold = new float[][]{{-1 , -1},
                                               {0.29f , 100},
                                               {0.29f , 100},
                                               {-1 , -1},
                                               {0.29f , 100},
                                               {0.29f , 100},
                                               {0.29f , 100},
                                               {0.29f , 100},
                                               {0.29f , 100},
                                               {0.35f , 100},
                                               {0.35f , 100},
                                               {0.29f , 100},
                                               {-1 , -1},
                                               {0.29f , 100},
                                               {0.29f , 100},
                                               {-1 , -1}};
       
        this.caliIndexMap.put("B12" , 14);
        this.caliIndexMap.put("C12" , 14);
        this.caliIndexMap.put("D12" , 14);
        this.caliIndexMap.put("E12" , 14);
        this.caliIndexMap.put("F12" , 14);
        this.caliIndexMap.put("G12" , 14);
        this.YStandard = new float[]{0.1f , 1.05f , 10f , 20f, 100f , 3f};
        
        
        
    }
    
}

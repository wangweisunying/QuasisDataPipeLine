/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quasisTest;

/**
 *
 * @author Wei Wang
 */
public class Upre extends QuasisTest{
    public Upre() {
        super();
        this.testName = "UPRE";
        this.testType = "UPRE";
        this.dataTable = "`vibrant_test_raw_data`.`inhalant_test`";
        this.negativeLocation = "D6";
        this.indexTestTitleArr = new String[]{
            "Phoma Betae",
"Dermatophagoides farinae",
"Dermatophagoides pteronyssinus",
"Mite Mix",
"Red Alder",
"Mucor Racemosus",
"Fusarium Moniliforme",
"Peanut",
"Timothy",
"Candida Albicans",
"Cat Epithelia",
"Australian Pine - Beefwood",
"Red Mulberry",
"Pecan",
"Ash Mix",
"Sheep Sorrel - Red Sorrel",
"Trichoderma Harzianum",
"Alternaria Alternata",
"Acacia",
"Blomia",
"Box elder",
"Ragweed, Short",
"Birch, White",
"Bahia, Grass",
"Nettle",
"Cockroach, American",
"Aspergillus fumigatus",
"Walnut Pollen, black",
"Helminthosporium, Soleni",
"Mugwort, common",
"Epicoccum Nigrum",
"Penicillium Chrysogenum",
"June/ky. Blue grass",
"Elm, American",
"Oak, White",
"Dog Epithelia",
"Johnson Grass",
"Red top grass",
"Russian Thistle",
"Rye, Perennial",
"Cedar Mountain",
"Cladosporium",
"Sycamore Am/Eastern",
"Bermuda",
"Oak, Black",
"Rhizopus, Stolonifer",
"Pigweed, rough"
};

        this.indexTestMapArr = new String[]{"Positive control",
"Phoma Betae",
"Dermatophagoides farinae",
"Dermatophagoides pteronyssinus",
"Mite Mix",
"Red Alder",
"Mucor Racemosus",
"Fusarium Moniliforme",
"Peanut",
"Positive control",
"Timothy",
"Candida Albicans",
"Cat Epithelia",
"Australian Pine - Beefwood",
"Red Mulberry",
"Pecan",
"Ash Mix",
"Sheep Sorrel - Red Sorrel",
"Trichoderma Harzianum",
"Alternaria Alternata",
"Acacia",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Positive control",
"Blomia",
"Box elder",
"Ragweed, Short",
"Birch, White",
"Bahia, Grass",
"Nettle",
"Cockroach, American",
"Aspergillus fumigatus",
"Walnut Pollen, black",
"Helminthosporium, Soleni",
"Mugwort, common",
"Epicoccum Nigrum",
"Penicillium Chrysogenum",
"June/ky. Blue grass",
"Elm, American",
"Oak, White",
"Dog Epithelia",
"Johnson Grass",
"Red top grass",
"Russian Thistle",
"Positive control",
"Rye, Perennial",
"Cedar Mountain",
"Cladosporium",
"Sycamore Am/Eastern",
"Bermuda",
"Oak, Black",
"Rhizopus, Stolonifer",
"Pigweed, rough",
"Positive control"

        };

        this.indexTestEquationOffset = new float[][]{
      {-1, -1},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {-1, -1},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {-1, -1},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {0, 0},
        {-1, -1}};




        this.indexTestUnitThreshold = new float[][]{{-1, -1},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {-1, -1},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {-1, -1},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {-1, -1},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {0.29f, 100},
        {-1, -1}};




        this.caliIndexMap.put("B6", 14);
        this.caliIndexMap.put("C6", 14);
        this.caliIndexMap.put("D6", 14);
        this.YStandard = new float[]{10f, 20f, 100f};

    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quasisdatapipeline;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.DataBaseCon;
import model.ExcelOperation;
import model.ExcelOperation.ExcelType;
import model.LXDataBaseCon;
import model.V7DataBaseCon;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import quasisTest.FoodAllergy12;
import quasisTest.FoodAllergy84;
import quasisTest.QuasisTest;
import quasisTest.Upre;

/**
 *
 * @author Wei Wang
 */
public class QuasisDataPipeLine {

    /**
     * @param args the command line arguments
     */
    private String pillarId = "UPRE80080010000003";
    String path = "D:\\QuasisData\\" + pillarId + ".xlsx";
    private String dataTable, testType, negativeLocation, testName , unitDataTable;
    private String[] indexTestMapArr, indexTestTitleArr;
    private float[] YStandard;
    private Map<String, Integer> caliIndexMap;
    private Map<String, Unit> rawMap;
    private float[][] indexTestEquationOffset;
    private float[][] indexTestUnitThreshold;
    private Map<String, List<String>> dupJunMap;
    private Map<String, float[]> igEMap;
    private Map<String, Unit> dupUnitMap;
    private Set<String> orderedBothJulienSet;
    private Map<Integer, List<Integer>> oldPanelIndex2NewPanelIndexMap;

    public static void main(String[] args) throws Exception {
      QuasisDataPipeLine test = new QuasisDataPipeLine(new Upre());
    //QuasisDataPipeLine test = new QuasisDataPipeLine(new FoodAllergy12());
   //  QuasisDataPipeLine test = new QuasisDataPipeLine(new FoodAllergy84());
        test.run();
//        test.writeToDB(test.path);
    }

    private void run() throws Exception {
        getRawData();
        getRefData();
        exportToExcel(path);
    }

    QuasisDataPipeLine(QuasisTest test) {
        this.unitDataTable = test.getUnitDataTable();
        this.indexTestEquationOffset = test.getIndexTestEquationOffset();
        this.indexTestUnitThreshold = test.getIndexTestUnitThreshold();
        this.dataTable = test.getDataTable();
        this.testType = test.getTestType();
        this.testName = test.getTestName();
        this.negativeLocation = test.getNegativeLocation();
        this.indexTestMapArr = test.getTestMapArr();
        this.indexTestTitleArr = test.getIndexTestTitleArr();
        this.caliIndexMap = test.getCaliIndexMap();
        this.YStandard = test.getYStandard();
        this.oldPanelIndex2NewPanelIndexMap = test.getOldPanelIndex2NewPanelIndex();
    }

    void writeToDB(String path) throws IOException, SQLException {

        DataBaseCon db = new V7DataBaseCon();
        Workbook wb = ExcelOperation.getReadConnection(path, ExcelType.XLSX);
        Sheet sheet = wb.getSheet("unit");
        int row = 1, col = 0;

        while (sheet.getRow(row) != null) {
            col = 0;
            Row rowCur = sheet.getRow(row++);
            if (rowCur.getCell(0).getStringCellValue().startsWith("D")) {
                continue;
            }
            List<Double> resultList = new ArrayList();
            String julien = rowCur.getCell(col++).getStringCellValue();
            String rowName = rowCur.getCell(col++).getStringCellValue();
            int colName = (int) rowCur.getCell(col++).getNumericCellValue();
            String pillarId = rowCur.getCell(col++).getStringCellValue();
            while (rowCur.getCell(col) != null) {
                resultList.add(rowCur.getCell(col++).getNumericCellValue());
            }

            for (int i = 0; i < resultList.size(); i++) {
                String sql = "insert into "+ unitDataTable +" (test_name,julien_barcode,unit, pillar_plate_id,row,col) values('"
                        + indexTestTitleArr[i] + "','" + julien + "','" + resultList.get(i) + "','" + pillarId + "','" + rowName + "'," + colName + ") on duplicate key update unit = '" + resultList.get(i) + "';";
                db.write(sql);
                System.out.println(sql);
            }
        }

        String sql1 = "UPDATE `vibrant_test_tracking`.`pillar_plate_info` SET `status`='finish' WHERE `pillar_plate_id`='" + pillarId + "';";
        db.write(sql1);
        String sql2 = "UPDATE `vibrant_test_tracking`.`pillar_info` SET `disease_name`='"+ testType +"', `test_type`='" + testType + "' WHERE `pillar_plate_id`='" + pillarId + "' and`chip_id`='0';";
        db.write(sql2);

        String sql3 = "insert into tsp_test_qc_data.test_qc_data (test_name,pillar_plate_id,cal_1,pos_ctrl_1,neg_ctrl_1,`time`) values ('" + testName + "','" + pillarId + "',1,1.5,0.05,now());";

        db.write(sql3);
        db.close();
    }

    private void getRawData() throws SQLException, Exception {
        rawMap = new HashMap();
        DataBaseCon db = new V7DataBaseCon();
        String sql = "select `index` , julien_barcode , row ,col , `signal`  from \n"
                + "(SELECT * FROM " + dataTable + " where pillar_plate_id like '" + pillarId + "_180 sec') as a  \n"
                + "left join\n"
                + "(select * from vibrant_test_tracking.well_info where well_plate_id = (select well_plate_id from  vibrant_test_tracking.pillar_plate_info where pillar_plate_id = '" + pillarId + "')) as b\n"
                + "on a.row = b.well_row and a.col = b.well_col order by julien_barcode,`index`;";
        System.out.println(sql);
        ResultSet rs = db.read(sql);
        while (rs.next()) {
            int index = rs.getInt(1);
            String julienBarcode = rs.getString(2);
            String row = rs.getString(3);
            int col = rs.getInt(4);
            float rawSignal = rs.getFloat(5);
            rawMap.computeIfAbsent(row + col, x -> new Unit(julienBarcode, pillarId, row, col, new float[indexTestMapArr.length])).setUnitValue(index, rawSignal);
        }
        db.close();
//        for (Unit unit : rawMap.values()) {
//            System.out.println(unit.julienBarcode + "   " + unit.row + "   " + unit.col + "   " + Arrays.toString(unit.unitArr));
//        }
    }

    private void getRefData() throws SQLException {
        dupJunMap = new HashMap();
        igEMap = new HashMap();
        dupUnitMap = new HashMap();
        orderedBothJulienSet = new HashSet();
        List<String> julienList = new ArrayList();
        for (Unit unit : rawMap.values()) {
            if (unit.getJulien() == null || Character.isLetter(unit.getJulien().charAt(0))) {
                continue;
            }
            julienList.add(unit.getJulien());
        }
        DataBaseCon db = new LXDataBaseCon();
        if (testName.startsWith("FAAE")) {
            StringBuilder sbJulien = new StringBuilder();
            for (Unit unit : rawMap.values()) {
                String julien = unit.getJulien();
//                System.out.println(julien);
                if(julien == null || Character.isLetter(julien.charAt(0)))continue;
                sbJulien.append(julien).append(",");
            }
            sbJulien.setLength(sbJulien.length() - 1);
            
            
            
            String dupSql = "SELECT \n" +
"    MAX(sd.sample_id) AS A, sd1.*\n" +
"FROM\n" +
"    vibrant_america_information.sample_data sd\n" +
"        JOIN\n" +
"    (SELECT \n" +
"        pd.patient_id,\n" +
"            COUNT(sd.sample_id) AS sample_count,\n" +
"            GROUP_CONCAT(sd.sample_id, ':', package_id_arr\n" +
"                ORDER BY sd.sample_id DESC),            \n" +
"            GROUP_CONCAT(julien_barcode\n" +
"                ORDER BY sd.sample_id DESC) as barcode,\n" +
"            GROUP_CONCAT(sample_collection_time\n" +
"                ORDER BY sd.sample_id DESC)\n" +
"    FROM\n" +
"        vibrant_america_information.sample_data sd\n" +
"    JOIN vibrant_america_information.selected_test_list stl ON stl.sample_id = sd.sample_id\n" +
"    JOIN vibrant_america_information.patient_details pd ON pd.patient_id = sd.patient_id\n" +
"    WHERE\n" +
"        (stl.Order_Allergy_Panel != 0\n" +
"            OR stl.Order_Food_Allergen_Panel1 != 0\n" +
"            OR stl.Order_Food_Allergen_Panel2 != 0\n" +
"            OR stl.Order_Food_Allergen_Panel3 != 0\n" +
"            OR stl.Order_Food_Allergen_Panel4 != 0\n" +
"            or stl.Order_Corn_Zoomer_Panel1 != 0\n" +
"			or stl.Order_Egg_Zoomer_Panel1 != 0\n" +
"			or stl.Order_Dairy_Zoomer_Panel1 != 0\n" +
"			or stl.Order_Peanut_Zoomer_Panel2 != 0\n" +
"            )\n" +
"            AND sd.customer_id < 999000\n" +
"    GROUP BY pd.patient_id\n" +
"    HAVING COUNT(sd.sample_id) > 1\n" +
"    ORDER BY sd.sample_id DESC) sd1 ON sd1.patient_id = sd.patient_id\n" +
"        JOIN\n" +
"    vibrant_america_information.selected_test_list stl ON stl.sample_id = sd.sample_id\n" +
"WHERE\n" +
"    (stl.Order_Allergy_Panel != 0\n" +
"        OR stl.Order_Food_Allergen_Panel1 != 0\n" +
"        OR stl.Order_Food_Allergen_Panel2 != 0\n" +
"        OR stl.Order_Food_Allergen_Panel3 != 0\n" +
"        OR stl.Order_Food_Allergen_Panel4 != 0\n" +
"        or stl.Order_Corn_Zoomer_Panel1 != 0\n" +
"        or stl.Order_Egg_Zoomer_Panel1 != 0\n" +
"        or stl.Order_Dairy_Zoomer_Panel1 != 0\n" +
"        or stl.Order_Peanut_Zoomer_Panel2 != 0\n" +
"        )\n" +
"        AND sd.customer_id < 999000\n" +
"        and ( \n" +
"	barcode in("+ sbJulien.toString() +")\n" +
"\n" +
") GROUP BY sd.patient_id\n" +
"ORDER BY A DESC;";
            System.out.println(dupSql);
            ResultSet rs = db.read(dupSql);
            while (rs.next()) {
                String tmp = rs.getString(5);
                for (String newJun : julienList) {
                    if (dupJunMap.containsKey(newJun)) {
                        continue;
                    }
                    if (tmp.contains(newJun)) {
                        for (String oldJun : tmp.split(",")) {
                            if (!newJun.equals(oldJun)) {
                                dupJunMap.computeIfAbsent(newJun, x -> new ArrayList()).add(oldJun);
                            }
                        }
                    }
                }
            }
            System.out.println(dupJunMap);
            if (!dupJunMap.isEmpty()) {
                for (List<String> oldJunList : dupJunMap.values()) {
                    for (String oldJun : oldJunList) {
                        julienList.add(oldJun);
                        dupUnitMap.put(oldJun, new Unit(oldJun));
                    }
                }

                StringBuilder sb = new StringBuilder();
                for (String oldJun : dupUnitMap.keySet()) {
                    sb.append(oldJun).append(",");
                }
                sb.setLength(sb.length() - 1);

                StringBuilder sbTitle = new StringBuilder();
                for (String test : indexTestMapArr) {
                    if (test.equals("Positive control")) {
                        sbTitle.append(-2).append(",");
                        continue;
                    }
                    sbTitle.append("`").append(test).append("`,");
                }
                sbTitle.setLength(sbTitle.length() - 1);

                String sqlDup = "select julien_barcode , " + sbTitle.toString() + "\n"
                        + "	from \n"
                        + "   vibrant_america_information.`patient_details` pd\n"
                        + "        JOIN\n"
                        + "    vibrant_america_information.`sample_data` sd ON sd.`patient_id` = pd.`patient_id`\n"
                        + "        join `vibrant_america_test_result`.`result_food_allergen_panel1` fa1 on fa1.sample_id = sd.sample_id\n"
                        + "        join `vibrant_america_test_result`.`result_food_allergen_panel2` fa2 on fa2.sample_id = sd.sample_id \n"
                        + "        join `vibrant_america_test_result`.`result_food_allergen_panel3` fa3 on fa3.sample_id = sd.sample_id \n"
                        + "        join `vibrant_america_test_result`.`result_food_allergen_panel4` fa4 on fa4.sample_id = sd.sample_id\n"
                        + "	where julien_barcode in (" + sb.toString() + ");";
                System.out.println(sqlDup);
                ResultSet rsDup = db.read(sqlDup);
                int col = rsDup.getMetaData().getColumnCount();
                while (rsDup.next()) {
                    String julien = rsDup.getString(1);
                    float[] unitArr = new float[indexTestMapArr.length];
                    for (int i = 2; i <= col; i++) {
                        unitArr[i - 2] = rsDup.getFloat(i) < 0 ? 0 : rsDup.getFloat(i);
                    }
                    dupUnitMap.get(julien).setUnitArr(unitArr);
                }

                //handle the old duplicate 
                sqlDup = "select julien_barcode , fa1.*\n"
                        + "	from \n"
                        + "   vibrant_america_information.`patient_details` pd\n"
                        + "        JOIN\n"
                        + "    vibrant_america_information.`sample_data` sd ON sd.`patient_id` = pd.`patient_id`\n"
                        + "        join `vibrant_america_test_result`.`Result_Allergy_Panel` fa1 on fa1.sample_id = sd.sample_id\n"
                        + "	where julien_barcode in (" + sb.toString() + ");";
                System.out.println(sqlDup);
                rsDup = db.read(sqlDup);
                col = rsDup.getMetaData().getColumnCount();
                while (rsDup.next()) {
                    String julien = rsDup.getString(1);

                    if (dupUnitMap.get(julien).getUnitArr() != null) {
                        continue;
                    }
                    float[] unitArr = new float[indexTestMapArr.length];
                    for (int i = 3; i <= col; i++) {
                        if (!oldPanelIndex2NewPanelIndexMap.containsKey(i)) {
                            continue;
                        }
                        for (int index : oldPanelIndex2NewPanelIndexMap.get(i)) {
                            unitArr[index] = rsDup.getFloat(i) < 0 ? 0 : rsDup.getFloat(i);
                        }
                    }

                    dupUnitMap.get(julien).setUnitArr(unitArr);
                }
            }
            for (Unit data : dupUnitMap.values()) {
                System.out.println(data.getJulien() + "    " + Arrays.toString(data.getUnitArr()));
                System.out.println(data.getUnitArr().length);
            }

            // start to handle the IGE map;
            StringBuilder sbAllJun = new StringBuilder();
            for (String jun : julienList) {
                sbAllJun.append(jun).append(",");
            }
            if (sbAllJun.length() != 0) {
                sbAllJun.setLength(sbAllJun.length() - 1);
                String sqlIge = "SELECT \n"
                        + "     sd.julien_barcode , c1.ige_2 , c2.ige_vw ,c3.org_val\n"
                        + "FROM\n"
                        + "    vibrant_america_information.sample_data AS sd\n"
                        + "    left join \n"
                        + "     `vibrant_america_test_result`.`instrument_internal_test_result` as C3 on C3.sample_id  = sd.sample_id  \n"
                        + " left JOIN\n"
                        + "    `vibrant_america_test_result`.`result_allergy_panel` AS C1 ON  C1.sample_id = sd.sample_id\n"
                        + " left join\n"
                        + " `vibrant_america_test_result`.`result_total_immunoglobulins_vw1` as C2 on C2.sample_id  = sd.sample_id \n"
                        + "    where C3.test_code = 'ige_2' and  sd.julien_barcode in (" + sbAllJun.toString() + ")\n"
                        + "    ;";
                System.out.println(sqlIge);
                ResultSet rsIge = db.read(sqlIge);
                while (rsIge.next()) {
                    String jun = rsIge.getString(1);
                    float[] ige = new float[]{rsIge.getFloat(2), rsIge.getFloat(3), rsIge.getFloat(4)};
                    igEMap.put(jun, ige);
                }
                System.out.println(dupJunMap);
//        System.out.println(dupUnitMap);
//        System.out.println(igEMap);

                String sqlOrderBothCheck = "select aa.julien_barcode from\n"
                        + "(SELECT \n"
                        + "    julien_barcode\n"
                        + "FROM\n"
                        + "    (SELECT \n"
                        + "        julien_barcode, test_id\n"
                        + "    FROM\n"
                        + "        vibrant_america_test_result.sample_test_result\n"
                        + "    WHERE\n"
                        + "        sample_id IN (SELECT \n"
                        + "                sample_id\n"
                        + "            FROM\n"
                        + "                vibrant_america_information.sample_data\n"
                        + "            WHERE\n"
                        + "                julien_barcode IN (" + sbAllJun.toString() + "))) AS a\n"
                        + "        JOIN\n"
                        + "    (SELECT \n"
                        + "        test_id\n"
                        + "    FROM\n"
                        + "        vibrant_america_information.test_list\n"
                        + "    WHERE\n"
                        + "        DI_group_name = 'FD_ALLER') AS b ON a.test_id = b.test_id group by julien_barcode) aa\n"
                        + "        JOIN\n"
                        + "(SELECT \n"
                        + "    julien_barcode\n"
                        + "FROM\n"
                        + "    (SELECT \n"
                        + "        julien_barcode, test_id\n"
                        + "    FROM\n"
                        + "        vibrant_america_test_result.sample_test_result\n"
                        + "    WHERE\n"
                        + "        sample_id IN (SELECT \n"
                        + "                sample_id\n"
                        + "            FROM\n"
                        + "                vibrant_america_information.sample_data\n"
                        + "            WHERE\n"
                        + "                julien_barcode IN (" + sbAllJun.toString() + "))) AS a\n"
                        + "        JOIN\n"
                        + "    (SELECT \n"
                        + "        test_id\n"
                        + "    FROM\n"
                        + "        vibrant_america_information.test_list\n"
                        + "    WHERE\n"
                        + "        DI_group_name = 'INHAL') AS b ON a.test_id = b.test_id group by julien_barcode) bb  on aa.julien_barcode = bb.julien_barcode ;";
                ResultSet rsOrder = db.read(sqlOrderBothCheck);
                while (rsOrder.next()) {
                    orderedBothJulienSet.add(rsOrder.getString(1));
                }
                System.out.println(orderedBothJulienSet);

            }

        } else if (testName.equals("UPRE")) {
            StringBuilder sbJulien = new StringBuilder();
            for (Unit unit : rawMap.values()) {
                String julien = unit.getJulien();
//                System.out.println(julien);
                if(julien == null || Character.isLetter(julien.charAt(0)))continue;
                sbJulien.append(julien).append(",");
            }
            sbJulien.setLength(sbJulien.length() - 1);
            
            
            
            String dupSql = "SELECT \n" +
"    MAX(sd.sample_id) AS A, sd1.*\n" +
"FROM\n" +
"    vibrant_america_information.sample_data sd\n" +
"        JOIN\n" +
"    (SELECT \n" +
"        pd.patient_id,\n" +
"            COUNT(sd.sample_id) AS sample_count,\n" +
"            GROUP_CONCAT(sd.sample_id, ':', package_id_arr\n" +
"                ORDER BY sd.sample_id DESC),            \n" +
"            GROUP_CONCAT(julien_barcode\n" +
"                ORDER BY sd.sample_id DESC) as barcode,\n" +
"            GROUP_CONCAT(sample_collection_time\n" +
"                ORDER BY sd.sample_id DESC)\n" +
"    FROM\n" +
"        vibrant_america_information.sample_data sd\n" +
"    JOIN vibrant_america_information.selected_test_list stl ON stl.sample_id = sd.sample_id\n" +
"    JOIN vibrant_america_information.patient_details pd ON pd.patient_id = sd.patient_id\n" +
"    WHERE\n" +
"        (stl.Order_Allergy_Panel != 0\n" +
"            OR stl.Order_Food_Allergen_Panel1 != 0\n" +
"            OR stl.Order_Food_Allergen_Panel2 != 0\n" +
"            OR stl.Order_Food_Allergen_Panel3 != 0\n" +
"            OR stl.Order_Food_Allergen_Panel4 != 0\n" +
"            or stl.Order_Corn_Zoomer_Panel1 != 0\n" +
"			or stl.Order_Egg_Zoomer_Panel1 != 0\n" +
"			or stl.Order_Dairy_Zoomer_Panel1 != 0\n" +
"			or stl.Order_Peanut_Zoomer_Panel2 != 0\n" +
"            )\n" +
"            AND sd.customer_id < 999000\n" +
"    GROUP BY pd.patient_id\n" +
"    HAVING COUNT(sd.sample_id) > 1\n" +
"    ORDER BY sd.sample_id DESC) sd1 ON sd1.patient_id = sd.patient_id\n" +
"        JOIN\n" +
"    vibrant_america_information.selected_test_list stl ON stl.sample_id = sd.sample_id\n" +
"WHERE\n" +
"    (stl.Order_Allergy_Panel != 0\n" +
"        OR stl.Order_Food_Allergen_Panel1 != 0\n" +
"        OR stl.Order_Food_Allergen_Panel2 != 0\n" +
"        OR stl.Order_Food_Allergen_Panel3 != 0\n" +
"        OR stl.Order_Food_Allergen_Panel4 != 0\n" +
"        or stl.Order_Corn_Zoomer_Panel1 != 0\n" +
"        or stl.Order_Egg_Zoomer_Panel1 != 0\n" +
"        or stl.Order_Dairy_Zoomer_Panel1 != 0\n" +
"        or stl.Order_Peanut_Zoomer_Panel2 != 0\n" +
"        )\n" +
"        AND sd.customer_id < 999000\n" +
"        and ( \n" +
"	barcode in("+ sbJulien.toString() +")\n" +
"\n" +
") GROUP BY sd.patient_id\n" +
"ORDER BY A DESC;";
            System.out.println(dupSql);
            ResultSet rs = db.read(dupSql);
            while (rs.next()) {
                String tmp = rs.getString(5);
                for (String newJun : julienList) {
                    if (dupJunMap.containsKey(newJun)) {
                        continue;
                    }
                    if (tmp.contains(newJun)) {
                        for (String oldJun : tmp.split(",")) {
                            if (!newJun.equals(oldJun)) {
                                dupJunMap.computeIfAbsent(newJun, x -> new ArrayList()).add(oldJun);
                            }
                        }
                    }
                }
            }
            System.out.println(dupJunMap);
            if (!dupJunMap.isEmpty()) {
                for (List<String> oldJunList : dupJunMap.values()) {
                    for (String oldJun : oldJunList) {
                        julienList.add(oldJun);
                        dupUnitMap.put(oldJun, new Unit(oldJun));
                    }
                }

                StringBuilder sb = new StringBuilder();
                for (String oldJun : dupUnitMap.keySet()) {
                    sb.append(oldJun).append(",");
                }
                sb.setLength(sb.length() - 1);

                StringBuilder sbTitle = new StringBuilder();
                for (String test : indexTestMapArr) {
                    if (test.equals("Positive control")) {
                        sbTitle.append(-2).append(",");
                        continue;
                    }
                    if (test.equals("Calibrator")) {
                        sbTitle.append("'").append(test).append("',");
                        continue;
                    }
                    sbTitle.append("`").append(test).append("`,");
                }
                sbTitle.setLength(sbTitle.length() - 1);

                String sqlDup = "select julien_barcode , "+ sbTitle.toString() +" from \n" +
"    vibrant_america_information.`sample_data` sd \n" +
"		left join `vibrant_america_test_result`.`result_inhalant_panel1` fa1 on fa1.sample_id = sd.sample_id\n" +
"        left join `vibrant_america_test_result`.`result_inhalant_panel2` fa2 on fa2.sample_id = sd.sample_id \n" +
"        left join `vibrant_america_test_result`.`result_inhalant_panel3` fa3 on fa3.sample_id = sd.sample_id \n" +
"	where julien_barcode in (" + sb.toString() +");";
                System.out.println(sqlDup);
                ResultSet rsDup = db.read(sqlDup);
                int col = rsDup.getMetaData().getColumnCount();
                while (rsDup.next()) {
                    String julien = rsDup.getString(1);
                    if(rsDup.getString(3) == null){
                        continue;
                    } 
                    float[] unitArr = new float[indexTestMapArr.length];
                    for (int i = 2; i <= col; i++) {
                        if(rsDup.getString(i).startsWith("Cali")) continue;
                        unitArr[i - 2] = rsDup.getFloat(i) < 0 ? 0 : rsDup.getFloat(i);
                    }
                    dupUnitMap.get(julien).setUnitArr(unitArr);
                }

                //handle the old duplicate 
                sqlDup = "select julien_barcode , fa1.*\n"
                        + "	from \n"
                        + "   vibrant_america_information.`patient_details` pd\n"
                        + "        JOIN\n"
                        + "    vibrant_america_information.`sample_data` sd ON sd.`patient_id` = pd.`patient_id`\n"
                        + "        join `vibrant_america_test_result`.`Result_Upper_Respiratory_Panel` fa1 on fa1.sample_id = sd.sample_id\n"
                        + "	where julien_barcode in (" + sb.toString() + ");";
                System.out.println(sqlDup);
                rsDup = db.read(sqlDup);
                col = rsDup.getMetaData().getColumnCount();
                while (rsDup.next()) {
                    String julien = rsDup.getString(1);

                    if (dupUnitMap.get(julien).getUnitArr() != null) {
                        continue;
                    }
                    float[] unitArr = new float[indexTestMapArr.length];
                    for (int i = 3; i <= col; i++) {
                        if (!oldPanelIndex2NewPanelIndexMap.containsKey(i)) {
                            continue;
                        }
                        for (int index : oldPanelIndex2NewPanelIndexMap.get(i)) {
                            unitArr[index] = rsDup.getFloat(i) < 0 ? 0 : rsDup.getFloat(i);
                        }
                    }

                    dupUnitMap.get(julien).setUnitArr(unitArr);
                }
            }
            for (Unit data : dupUnitMap.values()) {
                System.out.println(data.getJulien() + "    " + Arrays.toString(data.getUnitArr()));
                System.out.println(data.getUnitArr().length);
            }

            // start to handle the IGE map;
            StringBuilder sbAllJun = new StringBuilder();
            for (String jun : julienList) {
                sbAllJun.append(jun).append(",");
            }
            if (sbAllJun.length() != 0) {
                sbAllJun.setLength(sbAllJun.length() - 1);
                String sqlIge = "SELECT \n"
                        + "     sd.julien_barcode , c1.ige_2 , c2.ige_vw ,c3.org_val\n"
                        + "FROM\n"
                        + "    vibrant_america_information.sample_data AS sd\n"
                        + "    left join \n"
                        + "     `vibrant_america_test_result`.`instrument_internal_test_result` as C3 on C3.sample_id  = sd.sample_id  \n"
                        + " left JOIN\n"
                        + "    `vibrant_america_test_result`.`result_allergy_panel` AS C1 ON  C1.sample_id = sd.sample_id\n"
                        + " left join\n"
                        + " `vibrant_america_test_result`.`result_total_immunoglobulins_vw1` as C2 on C2.sample_id  = sd.sample_id \n"
                        + "    where C3.test_code = 'ige_2' and  sd.julien_barcode in (" + sbAllJun.toString() + ")\n"
                        + "    ;";
                System.out.println(sqlIge);
                ResultSet rsIge = db.read(sqlIge);
                while (rsIge.next()) {
                    String jun = rsIge.getString(1);
                    float[] ige = new float[]{rsIge.getFloat(2), rsIge.getFloat(3), rsIge.getFloat(4)};
                    igEMap.put(jun, ige);
                }
                System.out.println(dupJunMap);
//        System.out.println(dupUnitMap);
//        System.out.println(igEMap);

                String sqlOrderBothCheck = "select aa.julien_barcode from\n"
                        + "(SELECT \n"
                        + "    julien_barcode\n"
                        + "FROM\n"
                        + "    (SELECT \n"
                        + "        julien_barcode, test_id\n"
                        + "    FROM\n"
                        + "        vibrant_america_test_result.sample_test_result\n"
                        + "    WHERE\n"
                        + "        sample_id IN (SELECT \n"
                        + "                sample_id\n"
                        + "            FROM\n"
                        + "                vibrant_america_information.sample_data\n"
                        + "            WHERE\n"
                        + "                julien_barcode IN (" + sbAllJun.toString() + "))) AS a\n"
                        + "        JOIN\n"
                        + "    (SELECT \n"
                        + "        test_id\n"
                        + "    FROM\n"
                        + "        vibrant_america_information.test_list\n"
                        + "    WHERE\n"
                        + "        DI_group_name = 'FD_ALLER') AS b ON a.test_id = b.test_id group by julien_barcode) aa\n"
                        + "        JOIN\n"
                        + "(SELECT \n"
                        + "    julien_barcode\n"
                        + "FROM\n"
                        + "    (SELECT \n"
                        + "        julien_barcode, test_id\n"
                        + "    FROM\n"
                        + "        vibrant_america_test_result.sample_test_result\n"
                        + "    WHERE\n"
                        + "        sample_id IN (SELECT \n"
                        + "                sample_id\n"
                        + "            FROM\n"
                        + "                vibrant_america_information.sample_data\n"
                        + "            WHERE\n"
                        + "                julien_barcode IN (" + sbAllJun.toString() + "))) AS a\n"
                        + "        JOIN\n"
                        + "    (SELECT \n"
                        + "        test_id\n"
                        + "    FROM\n"
                        + "        vibrant_america_information.test_list\n"
                        + "    WHERE\n"
                        + "        DI_group_name = 'INHAL') AS b ON a.test_id = b.test_id group by julien_barcode) bb  on aa.julien_barcode = bb.julien_barcode ;";
                ResultSet rsOrder = db.read(sqlOrderBothCheck);
                System.out.println(sqlOrderBothCheck);
                while (rsOrder.next()) {
                    orderedBothJulienSet.add(rsOrder.getString(1));
                }
                System.out.println(orderedBothJulienSet);

            }

        }
        db.close();
    }

    private void exportToExcel(String path) throws IOException, Exception {

        Workbook wb = ExcelOperation.getWriteConnection(ExcelType.XLSX);
        int rowCt = 2, colCt = 0;
        Sheet sheet = wb.createSheet("raw");
//        sheet.createFreezePane(1, 0);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        CellStyle cs = wb.createCellStyle();
        cs.setFillForegroundColor(IndexedColors.PINK.getIndex());
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row rowTitle = sheet.createRow(0);
        rowTitle.createCell(colCt++).setCellValue("JulienBarcode");
        rowTitle.createCell(colCt++).setCellValue("row");
        rowTitle.createCell(colCt++).setCellValue("col");
        rowTitle.createCell(colCt++).setCellValue("pillarId");
        rowTitle.createCell(colCt++).setCellValue("Row LOD low");
        rowTitle.createCell(colCt++).setCellValue("Row LOD high");
        for (int i = 0; i < indexTestMapArr.length; i++) {
            sheet.autoSizeColumn(colCt);
            rowTitle.createCell(colCt++).setCellValue(indexTestMapArr[i]);
        }

        //create Negative
        Row rowNeg = sheet.createRow(1);
        rowNeg.createCell(0).setCellValue("Negative Ctril");
        colCt = 5;
        for (float negSignal : rawMap.get(negativeLocation).getUnitArr()) {
            rowNeg.createCell(++colCt).setCellValue(negSignal);
        }
        for (Unit unit : rawMap.values()) {
            if (unit.getJulien() == null) {
                continue;
            }
            colCt = 0;
            Row curRow = sheet.createRow(rowCt++);
            curRow.createCell(colCt++).setCellValue(unit.getJulien());
            curRow.createCell(colCt++).setCellValue(unit.getRow());
            curRow.createCell(colCt++).setCellValue(unit.getCol());
            curRow.createCell(colCt++).setCellValue(unit.getPillarId());
            curRow.createCell(colCt++).setCellValue(0.27);
            curRow.createCell(colCt++).setCellValue(100);
            float[] arr = unit.getUnitArr();
            for (int i = 0; i < arr.length; i++) {
                curRow.createCell(colCt++).setCellValue(arr[i]);
            }
        }

        int rowParameter = rowCt + 1;

        // equation formation
        rowCt += 10;
        colCt = 0;
        Row chartTitleRow = sheet.createRow(rowCt++);
        chartTitleRow.createCell(colCt++).setCellValue("X");
        chartTitleRow.createCell(colCt++).setCellValue("Y");
        chartTitleRow.createCell(colCt++).setCellValue("ln(Y)");
        chartTitleRow.createCell(colCt++).setCellValue("Y = A*EXP(B*X)");

        int eRow = rowCt;
        int eCol = colCt;

        chartTitleRow.createCell(colCt++).setCellValue("A");
        chartTitleRow.createCell(colCt++).setCellValue("B");
        chartTitleRow.createCell(colCt++).setCellValue("Rsquare");

        int yIndex = 0;
        int dataStartRow = rowCt + 1;
        for (String loc : caliIndexMap.keySet()) {
            colCt = 0;
            Row rowTmp = sheet.createRow(rowCt++);
            rowTmp.createCell(colCt++).setCellValue(rawMap.get(loc).getUnitArr()[caliIndexMap.get(loc)]);
            rowTmp.createCell(colCt++).setCellValue(YStandard[yIndex++]);
            String cellFormula = "LN(" + ExcelOperation.transferIntgerToString(colCt) + rowCt + ")";
            rowTmp.createCell(colCt).setCellFormula(cellFormula);
        }
        int dataEndRow = rowCt;
        int tmpCol = eCol;
        Row equationRow = sheet.getRow(eRow);
        String tmpData = "C" + dataStartRow + ":C" + dataEndRow + ",A" + dataStartRow + ":A" + dataEndRow;
        String AFormula = "EXP(INTERCEPT(" + tmpData + "))";
        equationRow.createCell(tmpCol++).setCellFormula(AFormula);
        String BFormula = "LINEST(" + tmpData + ",TRUE,FALSE)";
        equationRow.createCell(tmpCol++).setCellFormula(BFormula);
        String RSQFormula = "RSQ(" + tmpData + ")";
        equationRow.createCell(tmpCol++).setCellFormula(RSQFormula);

        //write the equation
        int aRow = rowParameter;
        int bRow = rowParameter + 1;
        Row rowParaA = sheet.createRow(rowParameter++);
        Row rowParaB = sheet.createRow(rowParameter++);

        int rowLOD = rowParameter;
        Row rowParaLODLow = sheet.createRow(rowParameter++);
        Row rowParaLODHigh = sheet.createRow(rowParameter++);

        int colParaStart = 6;

        int indexTestUnitThresholdIndex = 0;
        for (float[] offset : indexTestEquationOffset) {
            if (offset[0] == -1 && offset[1] == -1) {
                ++colParaStart;
                ++indexTestUnitThresholdIndex;
                continue;
            }
            String formulaA = ExcelOperation.transferIntgerToString(eCol + 1) + (eRow + 1) + " + " + offset[0];
            String formulaB = ExcelOperation.transferIntgerToString(eCol + 2) + (eRow + 1) + " + " + offset[1];
            rowParaA.createCell(colParaStart).setCellFormula(formulaA);
            rowParaB.createCell(colParaStart).setCellFormula(formulaB);
            rowParaLODLow.createCell(colParaStart).setCellValue(indexTestUnitThreshold[indexTestUnitThresholdIndex][0]);
            rowParaLODHigh.createCell(colParaStart).setCellValue(indexTestUnitThreshold[indexTestUnitThresholdIndex][1]);
            ++colParaStart;
            ++indexTestUnitThresholdIndex;
        }

        // generate Unit Data 
        // need Duplicate Data infomation and realated total IGE
        boolean[] blankOut = new boolean[6 + indexTestUnitThreshold.length];
        for (int i = 0; i < indexTestUnitThreshold.length; i++) {
            if (indexTestUnitThreshold[i][0] == -1) {
                blankOut[i + 6] = true;
            }
        }

        // handle title
        Sheet sheetUnit = wb.createSheet("unit");
//        sheetUnit.createFreezePane(1, 0);

        int unitRowCt = 0;
        int unitColCt = 0;
        rowCt = 0;
        colCt = 0;

        Row unitTitle = sheetUnit.createRow(unitRowCt++);

        while (sheet.getRow(rowCt).getCell(colCt) != null) {
            if (colCt == 4 || colCt == 5) {
                ++colCt;
                continue;
            }
            if (!blankOut[colCt]) {
                String titleF = "raw!" + ExcelOperation.transferIntgerToString(colCt + 1) + (rowCt + 1);
                unitTitle.createCell(unitColCt).setCellFormula(titleF);
                ++unitColCt;
            }
            ++colCt;
        }
        unitTitle.createCell(unitColCt + 1).setCellValue("TotalIGE2");
        unitTitle.createCell(unitColCt + 2).setCellValue("TotalIGEVW");
        unitTitle.createCell(unitColCt + 3).setCellValue("OrgValue");
        unitTitle.createCell(unitColCt + 4).setCellValue(" >= 0.35");
        unitTitle.createCell(unitColCt + 5).setCellValue(" < 0.35 && >= 0.05 ");

        // handle data
        colCt = 0;
        rowCt = 2;
        int negativeRowCt = 1;

        while (sheet.getRow(rowCt) != null) {
            colCt = 0;
            unitColCt = 4;
            Row curRow = sheet.getRow(rowCt);
            String julien = curRow.getCell(colCt).getStringCellValue();
            if (!Character.isDigit(julien.charAt(0))) {
                rowCt++;
                continue;
            }

            Row curUnitRow = sheetUnit.createRow(unitRowCt++);
            for (int i = 0; i < 4; i++) {
                String formula = "raw!" + ExcelOperation.transferIntgerToString(colCt + 1) + (rowCt + 1);
                Cell cell = curUnitRow.createCell(i);
                cell.setCellFormula(formula);
                evaluator.evaluateInCell(cell);

                if (i == 0 && orderedBothJulienSet.contains(cell.getStringCellValue())) {
                    cell.setCellStyle(cs);
                }
                ++colCt;
            }
            colCt += 2;
            for (int i = 0; i < indexTestUnitThreshold.length; i++) {
                if (!blankOut[colCt]) {
                    //=IF(IF(2 * raw!F3 <=  raw!F2, 0.05, 2*raw!F98*EXP(raw!F99*raw!F3)) <0.129,0.05,IF(IF(2 * raw!F3 <=  raw!F2, 0.05, 2*raw!F98*EXP(raw!F99*raw!F3)) >105, 100, IF(2 * raw!F3 <=  raw!F2, 0.05, 2*raw!F98*EXP(raw!F99*raw!F3))))
                    String colChar = ExcelOperation.transferIntgerToString(colCt + 1);
                    String raw = "raw!" + colChar + (rowCt + 1);
                    String neg = "raw!" + colChar + (negativeRowCt + 1);
                    String lowLOD = "raw!" + colChar + (rowLOD + 1);
                    String highLOD = "raw!" + colChar + (rowLOD + 2);

                    String rowLowLOD = "raw!E" + (rowCt + 1);
                    String rowHighLOD = "raw!F" + (rowCt + 1);
                    String unitTras = "IF(2*" + raw + "<=" + neg + ",0.05," + "2*raw!" + colChar + (aRow + 1) + "*EXP(raw!" + colChar + (bRow + 1) + "*" + raw + "))";
                    String finalForm = "IF(" + unitTras + "<" + lowLOD + ",0.05,IF(" + unitTras + ">" + highLOD + ",100," + unitTras + "))";
                    finalForm = "IF(" + finalForm + "<" + rowLowLOD + ",0.05,IF(" + finalForm + ">" + rowHighLOD + ",100," + finalForm + "))";

                    curUnitRow.createCell(unitColCt++).setCellFormula(finalForm);
                }
                ++colCt;

            }
            //ige
            if (igEMap.containsKey(julien)) {
                System.out.println(Arrays.toString(igEMap.get(julien)));
                curUnitRow.createCell(unitColCt + 1).setCellValue(igEMap.get(julien)[0]);
                curUnitRow.createCell(unitColCt + 2).setCellValue(igEMap.get(julien)[1]);
                curUnitRow.createCell(unitColCt + 3).setCellValue(igEMap.get(julien)[2]);
            }
            rowCt++;

            //count
            int rowCount = unitRowCt;
            String colCount = ExcelOperation.transferIntgerToString(unitColCt);
            String formula1 = "COUNTIF(E" + rowCount + ":" + colCount + rowCount + ",\">=0.35\")";
            String formula2 = "COUNT(E" + rowCount + ":" + colCount + rowCount + ")-" + ExcelOperation.transferIntgerToString(unitColCt + 5) + rowCount;
            curUnitRow.createCell(unitColCt + 4).setCellFormula(formula1);
            curUnitRow.createCell(unitColCt + 5).setCellFormula(formula2);

            //        System.out.println(dupJunMap);
//        System.out.println(dupUnitMap);
//        System.out.println(igEMap);
//             insert duplicate data
            if (dupJunMap.containsKey(julien)) {
                for (String dupJun : dupJunMap.get(julien)) {
                    Row curDupRow = sheetUnit.createRow(unitRowCt++);
                    unitColCt = 0;
                    Unit dupUnit = dupUnitMap.get(dupJun);
                    curDupRow.createCell(unitColCt++).setCellValue(dupUnit.getJulien());
                    curDupRow.createCell(unitColCt++).setCellValue(dupUnit.getRow());
                    curDupRow.createCell(unitColCt++).setCellValue(dupUnit.getCol());
                    curDupRow.createCell(unitColCt++).setCellValue(dupUnit.getPillarId());
                    float[] arr = dupUnit.getUnitArr();
                    for (int i = 0; i < arr.length - 1; i++) {
                        if (!blankOut[i + 4]) {
                            curDupRow.createCell(unitColCt++).setCellValue(arr[i]);
                        }
                    }

                    //ige
                    if (igEMap.containsKey(dupJun)) {
                        curDupRow.createCell(unitColCt + 1).setCellValue(igEMap.get(dupJun)[0]);
                        curDupRow.createCell(unitColCt + 2).setCellValue(igEMap.get(dupJun)[1]);
                    }

                    //count
                    rowCount = ++rowCount;
                    colCount = ExcelOperation.transferIntgerToString(unitColCt);
                    formula1 = "COUNTIF(E" + rowCount + ":" + colCount + rowCount + ",\">=0.35\")";
                    formula2 = "COUNT(E" + rowCount + ":" + colCount + rowCount + ")-" + ExcelOperation.transferIntgerToString(unitColCt + 5) + rowCount;
                    curDupRow.createCell(unitColCt + 4).setCellFormula(formula1);
                    curDupRow.createCell(unitColCt + 5).setCellFormula(formula2);

                }
            }

        }
        String range = "E2:" + ExcelOperation.transferIntgerToString(unitColCt) + (unitRowCt);
        System.out.println(range);
        ExcelOperation.setConditionalFormatting(sheetUnit, IndexedColors.RED, ComparisonOperator.GT, new String[]{"10"}, range);
        ExcelOperation.setConditionalFormatting(sheetUnit, IndexedColors.YELLOW, ComparisonOperator.BETWEEN, new String[]{"0.8", "10"}, range);
        ExcelOperation.setConditionalFormatting(sheetUnit, IndexedColors.GREEN, ComparisonOperator.LT, new String[]{"0.8"}, range);

        ExcelOperation.writeExcel(path, wb);
        File file = new File(path);
        Desktop.getDesktop().open(file);
    }

}

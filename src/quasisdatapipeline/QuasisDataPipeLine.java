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
import java.util.List;
import java.util.Map;
import model.DataBaseCon;
import model.ExcelOperation;
import model.ExcelOperation.ExcelType;
import model.LXDataBaseCon;
import model.V7DataBaseCon;
import org.apache.poi.ss.usermodel.ComparisonOperator;
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
    private String pillarId = "UPRE80070010000011";
    String path = "C:\\Users\\Wei Wang\\Desktop\\FAAEDATA\\testOut\\"+ pillarId +".xlsx";
    private String dataTable, testType, negativeLocation , testName;
    private String[] indexTestMapArr , indexTestTitleArr ;
    private float[] YStandard;
    private Map<String, Integer> caliIndexMap;
    private Map<String, Unit> rawMap;
    private float[][] indexTestEquationOffset;
    private float[][] indexTestUnitThreshold;
    private Map<String , List<String>> dupJunMap;
    private Map<String , float[]> igEMap;
    private Map<String , Unit> dupUnitMap;
    
    
    
    
    public static void main(String[] args) throws Exception {
        QuasisDataPipeLine test = new QuasisDataPipeLine(new Upre());
        test.run();
//        test.writeToDB(test.path);
    }
    private void run() throws Exception{
        getRawData();
        getRefData();
        exportToExcel(path);
    }
    
    
    

    QuasisDataPipeLine(QuasisTest test) {
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
    }


    void writeToDB(String path) throws IOException, SQLException{
        
        DataBaseCon db = new V7DataBaseCon();
        
        Workbook wb = ExcelOperation.getReadConnection(path, ExcelType.XLSX);
        Sheet sheet = wb.getSheet("unit");
        int row = 1 , col = 0;
       
        while(sheet.getRow(row)!= null){
            col = 0;
            Row rowCur = sheet.getRow(row++);
            if(rowCur.getCell(0).getStringCellValue().startsWith("D")){
                continue;
            }
            List<Double> resultList = new ArrayList();
            String julien = rowCur.getCell(col++).getStringCellValue();
            String rowName = rowCur.getCell(col++).getStringCellValue();
            int colName = (int)rowCur.getCell(col++).getNumericCellValue();
            String pillarId = rowCur.getCell(col++).getStringCellValue();
            while(rowCur.getCell(col)!= null){
                resultList.add(rowCur.getCell(col++).getNumericCellValue());
            }

            for(int i = 0 ; i < resultList.size() ; i++){
                String sql = "insert into tsp_test_unit_data.faae_unit_data (test_name,julien_barcode,unit, pillar_plate_id,row,col) values('"+
                        indexTestTitleArr[i]+"','"+ julien +"','"+ resultList.get(i) +"','"+ pillarId +"','"+ rowName +"',"+ colName +") on duplicate key update unit = '"+ resultList.get(i) +"';";
                db.write(sql);
                System.out.println(sql);
            }
        }
        
        String sql1 = "UPDATE `vibrant_test_tracking`.`pillar_plate_info` SET `status`='finish' WHERE `pillar_plate_id`='"+ pillarId +"';";
        db.write(sql1);
        String sql2 = "UPDATE `vibrant_test_tracking`.`pillar_info` SET `disease_name`='Food_Allergy_12', `test_type`='"+ testType +"' WHERE `pillar_plate_id`='"+ pillarId +"' and`chip_id`='0';";
        db.write(sql2);

        String sql3 = "insert into tsp_test_qc_data.test_qc_data (test_name,pillar_plate_id,cal_1,pos_ctrl_1,neg_ctrl_1,`time`) values ('"+ testName +"','"+ pillarId +"',1,1.5,0.05,now());";
                
        db.write(sql3);
        db.close();
    }
    
    
    
    private void getRawData() throws SQLException, Exception {
        rawMap = new HashMap();
        DataBaseCon db = new V7DataBaseCon();
        String sql = "select `index` , julien_barcode , row ,col , `signal`  from \n"
                + "(SELECT * FROM " + dataTable + " where pillar_plate_id like '" + pillarId + "_300 sec') as a  \n"
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
    
    private void getRefData() throws SQLException{
        dupJunMap = new HashMap();
        igEMap = new HashMap();
        dupUnitMap = new HashMap();
        
        List<String> julienList = new ArrayList();
        for (Unit unit : rawMap.values()) {
            if(unit.getJulien() == null || Character.isLetter(unit.getJulien().charAt(0))){
                continue;
            }
            julienList.add(unit.getJulien());
        }
        
        DataBaseCon db = new LXDataBaseCon();
        String dupSql = "SELECT\n" +
"   group_concat(sd.julien_barcode order by sd.julien_barcode desc)\n" +
"FROM\n" +
"    vibrant_america_information.`patient_details` pd\n" +
"        JOIN\n" +
"    vibrant_america_information.`sample_data` sd ON sd.`patient_id` = pd.`patient_id`\n" +
"        JOIN\n" +
"    vibrant_america_information.`customers_of_patients` cop ON cop.`patient_id` = sd.`patient_id`\n" +
"        AND cop.`customer_id` = sd.`customer_id`\n" +
"        join\n" +
"          vibrant_america_information.`customer_details` cd on  cd.customer_id = sd.customer_id\n" +
"        AND cop.`customer_id` = sd.`customer_id`\n" +
"        join vibrant_america_information.selected_test_list slt on slt.sample_id = sd.sample_id\n" +
"        join `vibrant_america_test_result`.`result_food_allergen_panel1` fa1 on fa1.sample_id = sd.sample_id\n" +
"        join `vibrant_america_test_result`.`result_food_allergen_panel2` fa2 on fa2.sample_id = sd.sample_id \n" +
"        join `vibrant_america_test_result`.`result_food_allergen_panel3` fa3 on fa3.sample_id = sd.sample_id \n" +
"        join `vibrant_america_test_result`.`result_food_allergen_panel4` fa4 on fa4.sample_id = sd.sample_id \n" +
"        \n" +
"WHERE\n" +
"	 (slt.Order_Food_Allergen_Panel1 != 0\n" +
"	OR slt.Order_Food_Allergen_Panel2 != 0\n" +
"	OR slt.Order_Food_Allergen_Panel3 != 0\n" +
"	OR slt.Order_Food_Allergen_Panel4 != 0)\n" +
"    and sd.`customer_id` < 999000\n" +
"   group by PD.PATIENT_ID having count(*)>=2  order by substring(group_concat(sd.julien_barcode order by sd.julien_barcode desc),1,10) desc;";
        ResultSet rs = db.read(dupSql);
        while(rs.next()){
            String tmp = rs.getString(1);
            for(String newJun : julienList ){
                if(dupJunMap.containsKey(newJun)){
                    continue;
                }
                if(tmp.contains(newJun)){
                    for(String oldJun : tmp.split(",")){
                        if(!newJun.equals(oldJun)){
                            dupJunMap.computeIfAbsent(newJun, x -> new ArrayList()).add(oldJun);
                        }
                    }
                }
            } 
        }
        
        if(!dupJunMap.isEmpty()){
            for(List<String> oldJunList : dupJunMap.values()){
                for(String oldJun : oldJunList){
                    julienList.add(oldJun);
                    dupUnitMap.put(oldJun, new Unit(oldJun));
                }
            }
            
            StringBuilder sb = new StringBuilder();
            for(String oldJun : dupUnitMap.keySet()){
                sb.append(oldJun).append(",");
            }
            sb.setLength(sb.length() - 1 );
            
            StringBuilder sbTitle = new StringBuilder();
            for(String test : indexTestMapArr){
                if(test.equals("Positive control")){
                    sbTitle.append(-2).append(",");
                    continue;
                }
                sbTitle.append("`").append(test).append("`,");
            }
            sbTitle.setLength(sbTitle.length() - 1 );
            
            
            
            
            String sqlDup = "select julien_barcode , "+ sbTitle.toString() +"\n" +
"	from \n" +
"   vibrant_america_information.`patient_details` pd\n" +
"        JOIN\n" +
"    vibrant_america_information.`sample_data` sd ON sd.`patient_id` = pd.`patient_id`\n" +
"        join `vibrant_america_test_result`.`result_food_allergen_panel1` fa1 on fa1.sample_id = sd.sample_id\n" +
"        join `vibrant_america_test_result`.`result_food_allergen_panel2` fa2 on fa2.sample_id = sd.sample_id \n" +
"        join `vibrant_america_test_result`.`result_food_allergen_panel3` fa3 on fa3.sample_id = sd.sample_id \n" +
"        join `vibrant_america_test_result`.`result_food_allergen_panel4` fa4 on fa4.sample_id = sd.sample_id\n" +
"	where julien_barcode in ("+ sb.toString() +");";
            System.out.println(sqlDup);
            ResultSet rsDup = db.read(sqlDup);
            int col = rsDup.getMetaData().getColumnCount();
            while(rsDup.next()){
                String julien = rsDup.getString(1);
                float[] unitArr = new float[indexTestMapArr.length];
                for(int i = 2 ; i <= col ; i++){
                    unitArr[i - 2] = rsDup.getFloat(i);
                }
                dupUnitMap.get(julien).setUnitArr(unitArr);
            }
        }
//        for(Unit data : dupUnitMap.values()){
//            System.out.println(data.getJulien() + "    " + Arrays.toString(data.getUnitArr()));
//        }
        
        // start to handle the IGE map;
        
        
        StringBuilder sbAllJun = new StringBuilder();
        for(String jun : julienList){
            sbAllJun.append(jun).append(",");
        }
        sbAllJun.setLength(sbAllJun.length() - 1);
        
        String sqlIge = "SELECT \n" +
"     sd.julien_barcode , c1.ige_2 , c2.ige_vw ,c3.org_val\n" +
"FROM\n" +
"    vibrant_america_information.sample_data AS sd\n" +
"    left join \n" +
"     `vibrant_america_test_result`.`instrument_internal_test_result` as C3 on C3.sample_id  = sd.sample_id  \n" +
"		left JOIN\n" +
"    `vibrant_america_test_result`.`result_allergy_panel` AS C1 ON  C1.sample_id = sd.sample_id\n" +
"		right join\n" +
"	`vibrant_america_test_result`.`result_total_immunoglobulins_vw1` as C2 on C2.sample_id  = sd.sample_id \n" +
"    where sd.julien_barcode in ("+ sbAllJun.toString() +")\n" +
"    ;";
        System.out.println(sqlIge);
        ResultSet rsIge = db.read(sqlIge);
        while(rsIge.next()){
            String jun = rsIge.getString(1);
            float[] ige = new float[]{rsIge.getFloat(2) , rsIge.getFloat(3) , rsIge.getFloat(4)};
            igEMap.put(jun , ige);
        }
        System.out.println(dupJunMap);
//        System.out.println(dupUnitMap);
//        System.out.println(igEMap);
    }
    
    
    private void exportToExcel(String path) throws IOException, Exception {

        Workbook wb = ExcelOperation.getWriteConnection(ExcelType.XLSX);
        int rowCt = 2, colCt = 0;
        Sheet sheet = wb.createSheet("raw");
        Row rowTitle = sheet.createRow(0);
        rowTitle.createCell(colCt++).setCellValue("JulienBarcode");
        rowTitle.createCell(colCt++).setCellValue("row");
        rowTitle.createCell(colCt++).setCellValue("col");
        rowTitle.createCell(colCt++).setCellValue("pillarId");
        for (int i = 0; i < indexTestMapArr.length; i++) {
            sheet.autoSizeColumn(colCt);
            rowTitle.createCell(colCt++).setCellValue(indexTestMapArr[i]);
        }
        
        

        //create Negative
        Row rowNeg = sheet.createRow(1);
        rowNeg.createCell(0).setCellValue("Negative Ctril");
        colCt = 3;
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
        
        int colParaStart = 4;
        
        for(float[] offset : indexTestEquationOffset){
            if(offset[0] == -1 && offset[1] == -1) {
                ++colParaStart;
                continue;
            }
            String formulaA = ExcelOperation.transferIntgerToString(eCol + 1) + (eRow + 1)  + " + " + offset[0];
            String formulaB = ExcelOperation.transferIntgerToString(eCol + 2) + (eRow + 1) + " + " + offset[1];;
            rowParaA.createCell(colParaStart).setCellFormula(formulaA);
            rowParaB.createCell(colParaStart).setCellFormula(formulaB);
            ++colParaStart;
        }
         
        
        // generate Unit Data 
        // need Duplicate Data infomation and realated total IGE
        
        
        boolean[] blankOut = new boolean[4 + indexTestUnitThreshold.length];
        for(int i = 0 ; i < indexTestUnitThreshold.length ; i++){
            if(indexTestUnitThreshold[i][0] == -1){
                blankOut[i + 4] = true;
            }
        }
        
        // handle title
        Sheet sheetUnit = wb.createSheet("unit");
        int unitRowCt = 0;
        int unitColCt = 0;
        rowCt = 0;
        colCt = 0;
        
        Row unitTitle = sheetUnit.createRow(unitRowCt++);
        
        while(sheet.getRow(rowCt).getCell(colCt) != null){
            if(!blankOut[colCt]){
                String titleF = "raw!" + ExcelOperation.transferIntgerToString(colCt + 1) + (rowCt + 1);
                unitTitle.createCell(unitColCt).setCellFormula(titleF);
                ++unitColCt;
            }
            ++colCt;
        }
        unitTitle.createCell(unitColCt + 1).setCellValue("TotalIGE2");
        unitTitle.createCell(unitColCt + 2).setCellValue("TotalIGEVW");
        unitTitle.createCell(unitColCt + 3).setCellValue("OrgValue");
        // handle data
        
        colCt = 0;
        rowCt = 2;
        int  negativeRowCt = 1;
        
        while(sheet.getRow(rowCt) != null){
            colCt = 0;
            unitColCt = 4;
            Row curRow = sheet.getRow(rowCt);
            String julien = curRow.getCell(colCt).getStringCellValue();
            if(!Character.isDigit(julien.charAt(0))){
                rowCt++;
                continue;
            }
            
            Row curUnitRow = sheetUnit.createRow(unitRowCt++);
            for(int i = 0 ; i < 4 ; i++){
                String formula = "raw!" + ExcelOperation.transferIntgerToString(colCt + 1) + (rowCt + 1);
                curUnitRow.createCell(i).setCellFormula(formula);
                ++colCt;
            }
            
            for (int i = 0; i < indexTestUnitThreshold.length; i++) {
                if (!blankOut[colCt]) {
                    //=IF(IF(2 * raw!F3 <=  raw!F2, 0.05, 2*raw!F98*EXP(raw!F99*raw!F3)) <0.129,0.05,IF(IF(2 * raw!F3 <=  raw!F2, 0.05, 2*raw!F98*EXP(raw!F99*raw!F3)) >105, 100, IF(2 * raw!F3 <=  raw!F2, 0.05, 2*raw!F98*EXP(raw!F99*raw!F3))))
                    String colChar = ExcelOperation.transferIntgerToString(colCt + 1);
                    String raw = "raw!" + colChar + (rowCt + 1);
                    String neg = "raw!" + colChar + (negativeRowCt + 1);
                    String unitTras = "IF(2*" + raw + "<=" + neg + ",0.05," + "2*raw!" + colChar + (aRow + 1) + "*EXP(raw!" + colChar + (bRow + 1) + "*" + raw + "))";
                    String finalForm = "IF(" + unitTras + "<" + indexTestUnitThreshold[i][0] + ",0.05,IF(" + unitTras + ">" + indexTestUnitThreshold[i][1] + ",100," + unitTras + "))";

                    curUnitRow.createCell(unitColCt++).setCellFormula(finalForm);
                }
                ++colCt;
            }
            //ige
            if(igEMap.containsKey(julien)){
                System.out.println(Arrays.toString(igEMap.get(julien)));
                curUnitRow.createCell(unitColCt + 1).setCellValue(igEMap.get(julien)[0]);
                curUnitRow.createCell(unitColCt + 2).setCellValue(igEMap.get(julien)[1]);
                curUnitRow.createCell(unitColCt + 3).setCellValue(igEMap.get(julien)[2]);
            }
            
            rowCt++;
            //        System.out.println(dupJunMap);
//        System.out.println(dupUnitMap);
//        System.out.println(igEMap);

//             insert duplicate data
            if(dupJunMap.containsKey(julien)){
                for(String dupJun : dupJunMap.get(julien)){
                    Row curDupRow = sheetUnit.createRow(unitRowCt++);
                    unitColCt = 0;
                    Unit dupUnit = dupUnitMap.get(dupJun);
                    curDupRow.createCell(unitColCt++).setCellValue(dupUnit.getJulien());
                    curDupRow.createCell(unitColCt++).setCellValue(dupUnit.getRow());
                    curDupRow.createCell(unitColCt++).setCellValue(dupUnit.getCol());
                    curDupRow.createCell(unitColCt++).setCellValue(dupUnit.getPillarId());
                    float[] arr = dupUnit.getUnitArr();
                    for(int i = 0 ; i < arr.length ;i++){
                        if(!blankOut[i + 4]){
                            curDupRow.createCell(unitColCt++).setCellValue(arr[i]);
                        }
                    }
                    
                    //ige
                    if(igEMap.containsKey(dupJun)){
                        
                        curDupRow.createCell(unitColCt + 1).setCellValue(igEMap.get(dupJun)[0]);
                        curDupRow.createCell(unitColCt + 2).setCellValue(igEMap.get(dupJun)[1]);
                    }
                    
                    
                }
            }
            
            
        }
        String range = "E2:" + ExcelOperation.transferIntgerToString(indexTestUnitThreshold.length) + (unitRowCt);
        ExcelOperation.setConditionalFormatting(sheetUnit, IndexedColors.RED, ComparisonOperator.GT, new String[]{"10"}, range);
        ExcelOperation.setConditionalFormatting(sheetUnit, IndexedColors.YELLOW, ComparisonOperator.BETWEEN, new String[]{"0.8" , "10"}, range);
        ExcelOperation.setConditionalFormatting(sheetUnit, IndexedColors.GREEN, ComparisonOperator.LT, new String[]{"0.8"}, range);       
        ExcelOperation.writeExcel(path, wb);
        
        File file = new File(path);
        Desktop.getDesktop().open(file);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quasisdatapipeline;

import java.io.IOException;
import java.sql.SQLException;
import quasisTest.FoodAllergy12;
import quasisTest.FoodAllergy84;
import quasisTest.Upre;

/**
 *
 * @author Wei Wang
 */
public class WriteToDB {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException {
        QuasisDataPipeLine test = new QuasisDataPipeLine(new Upre());
//        QuasisDataPipeLine test = new QuasisDataPipeLine(new FoodAllergy12());
//        QuasisDataPipeLine test = new QuasisDataPipeLine(new FoodAllergy84());
        System.out.println(test.path);
        test.writeToDB(test.path);
    }
    
}

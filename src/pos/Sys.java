/*
    Point of Sale System Project
    Authors: Clayton Barber, Brandon Barton, Declan Brennan, Maximilian Hasselbusch, Eric Metcalf
    Last Updated: 20 November 2015
 */
package pos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static pos.PointofSale.system;

public class Sys {

    boolean loggedIn;
    String error;
    Integer eid;
    String name;
    boolean isManager;
    Sale s;
    Connection con;
    private PrintStream ps;

    Sys(Connection con) {
        loggedIn = false;
        s = null;
        error = "No error";
        this.con = con;
    }

    public int login(String username, String password) {
        try {
            Statement smt = con.createStatement();
            String query = "select * from pos.employees where username = '" + username
                    + "' and password = '" + password + "'";
            ResultSet res = smt.executeQuery(query);
            if (res.next()) {
                name = res.getString("name") + " " + res.getString("surname");
                eid = res.getInt("eid");
                byte isLoggedIn = res.getByte("islogin");
                byte isMan = res.getByte("manPriv");
                if(isLoggedIn == 1){
                    return 3; //where 3 means already logged in
                }
                else smt.executeUpdate("update pos.employees set islogin=1 where eid ='" + eid + "'");
                if (isMan == 1) {
                    loggedIn = true;
                    isManager = true;
                    return 2;
                }
                else{
                    loggedIn = true;
                    isManager = false;
                    return 1;
                }
            } else {
                return 0;
            }
        } catch (SQLException sqe) {
            System.err.println("Unable to find requested entries");
            return 0;
        }
    }

    public void logout() {
        // saves games sku and price to text file
        try {
            Statement st = con.createStatement();
            ResultSet result = st.executeQuery("select sku, price from pos.games order by sku");
            File file = new File("games.txt");
            try {
                String sku;
                FileWriter writer = new FileWriter(file);
                BufferedWriter bwriter = new BufferedWriter(writer);
                while (result.next()) {
                    //result.next();
                    sku = String.format("%08d", result.getInt(1));
                    bwriter.write(sku + "=" + result.getDouble(2));
                    bwriter.newLine();
                }
                bwriter.close();
            } catch (IOException e) {
                System.err.println("File input error: " + e.getMessage());
            }
        } catch (SQLException e)    {
            System.err.println("Unable to create database text file." + e.getMessage());
        }
        try {
            Statement st = con.createStatement();
            st.executeUpdate("update pos.employees set islogin = 0 where eid = '" + eid + "'");
        } catch (SQLException se) {
            System.err.println("Unable to log user out of system.");
        }
        LoginNew l = new LoginNew(this, con);
    }

    public void buildSale() {
        s = new Sale(con, ps);
    }

    public String getError() {
        return error;
    }

    public void addEmployee() {
        Employee e = new Employee(con, this);
        e.addEmployee();
    }

    void removeEmployee() {
        Employee e = new Employee(con, this);
        e.removeEmployee();
    }

    void editManPrivilege() {
        Employee e = new Employee(con, this);
        e.editManPrivilege();
    }

    void changePassword() {
        Employee e = new Employee(con, this);
        e.changePassword();
    }

    void addItem() {
        AdministrativeFunctions af = new AdministrativeFunctions(con);
        af.addItem();
    }

    void InventoryLevels() {
        AdministrativeFunctions af = new AdministrativeFunctions(con);
        af.InventoryLevels();
    }

    void removeItem() {
        AdministrativeFunctions af = new AdministrativeFunctions(con);
        af.removeItem();
    }

    void editPrice() {
        AdministrativeFunctions af = new AdministrativeFunctions(con);
        af.updatePrice();
    }

    void viewSales() {
        SalesFunctions sf = new SalesFunctions(con);
        sf.viewSales();
    }

    void voidTransaction() {
        SalesFunctions sf = new SalesFunctions(con);
        sf.voidTransaction();
    }

    void updateDatabase() {
        File file = new File("towrite.txt");
        if (file.exists()) {
            try {
                FileInputStream fs = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fs));
                while (br.ready()) {
                    String line = br.readLine();
                    //System.err.println(line);
                    Statement us = con.createStatement();
                    us.executeUpdate(line);
                }
                br.close();
                file.delete();
            } catch (FileNotFoundException ex) {
                System.err.println("Unable to find file");
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            } catch (SQLException ex) {
                System.err.println("SQL Problems: " + ex.getMessage());
            }
        }
    }
}

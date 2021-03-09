/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pvehiculos3;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.bson.Document;

/**
 *
 * @author oracle
 */
public class Pvehiculos3 {
    
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static Connection conxOrcl;
    private static MongoClient mongoC;
    private static MongoDatabase mongoDB;

    /**
     * @param args the command line arguments
     */
    
    public static void getSQLConnection() throws SQLException{
        String usuario = "hr";
        String password = "hr";
        String host = "localhost"; 
        String puerto = "1521";
        String sid = "orcl";
        String url = "jdbc:oracle:thin:" + usuario + "/" + password + "@" + host + ":" + puerto + ":" + sid;
        
           
            conxOrcl = DriverManager.getConnection(url);
    }
    
    public static void connectMongoClient(){
        mongoC = new MongoClient("localhost");
    }
    
    public static void connectMongoDB(String name){
        mongoDB = mongoC.getDatabase(name);
    }
    
    public static MongoCollection<Document> getCollection(String name){
        return mongoDB.getCollection(name);
    }
    
    public static void getObjects(){
        emf = Persistence.createEntityManagerFactory("examen_files/vehicli.odb");
        em = emf.createEntityManager();
    }
    
    public static ArrayList<Vehiculos> getVehiculos(List<Vendas> ventas){
        ArrayList<Vehiculos> veh = new ArrayList<>();
        try{
            PreparedStatement pst = conxOrcl.prepareStatement("select * from vehiculos where idv=?");
            ResultSet rs;
            Vehiculos v;
            Struct st;
            BigDecimal tempPrecio1, tempMatricula2;
            Object[] campos;
            for(int i = 0; i < ventas.size(); i++){
                pst.setString(1, ventas.get(i).getCodvh());
                rs=pst.executeQuery();
                while(rs.next()){
                    st = (Struct)rs.getObject(3);
                    campos = st.getAttributes();
                    tempPrecio1 = (BigDecimal)campos[0];
                    tempMatricula2 = (BigDecimal)campos[1];
                    v = new Vehiculos(rs.getString(1), rs.getString(2),tempPrecio1.doubleValue(),tempMatricula2.doubleValue());
                    veh.add(v);
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return veh;
    }
    
    public static ArrayList<Clientes> getClientes(List<Vendas> ventas){
         ArrayList<Clientes> cli = new ArrayList<>();
        try{
            PreparedStatement pst = conxOrcl.prepareStatement("select * from clientes where idcli=?");
            ResultSet rs;
            Clientes c;
            Struct st;
            BigDecimal tempNCompras;
            String nomb;
            Object[] campos;
            for(int i = 0; i < ventas.size(); i++){
                pst.setString(1, ventas.get(i).getDni());
                rs=pst.executeQuery();
                while(rs.next()){
                    st = (Struct)rs.getObject(2);
                    campos = st.getAttributes();
                    nomb = (String)campos[0];
                    tempNCompras = (BigDecimal)campos[1];
                    c = new Clientes(rs.getString(1), nomb,tempNCompras.doubleValue());
                    cli.add(c);
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return cli;
    }
    
    public static List<Vendas> getVentas(){
        
        List<Vendas> ventas;
        getObjects();
        TypedQuery<Vendas> q1 = em.createQuery("SELECT v from Vendas v", Vendas.class);
        ventas = q1.getResultList();
        return ventas;
    }
    
    public static void saveInfo(List<Vendas> v, ArrayList<Clientes> cli, ArrayList<Vehiculos> veh){
        connectMongoClient();
        connectMongoDB("test");
        MongoCollection<Document> db=getCollection("finalveh");
        int pf=0;
        double temp;
        for(Vendas vs: v){
            for(Clientes c: cli){
                if(vs.getDni().equals(c.getDni())){
                    for(Vehiculos vehi: veh){
                        if(vs.getCodvh().equals(vehi.getCodveh())){
                            temp=vs.getTasas();
                            if(c.getNcompras() > 0){
                                pf=vehi.getPrezoorixe()-((2019-vehi.getAnomatricula())*500)-500+(int)temp;
                            }else{
                                pf=vehi.getPrezoorixe()-((2019-vehi.getAnomatricula())*500)+(int)temp;
                                ;
                            }
                            Document doc = new Document("_id",vs.getId()).append("dni", vs.getDni()).append("nome", c.getNomec()).append("coche", vehi.getNomveh()).append("precio", pf);
                            db.insertOne(doc);
                        }
                    }
                }
            }
        }
        
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        List<Vendas> ventas;
        ArrayList<Clientes> cli;
        ArrayList<Vehiculos> vehi;
        ventas = getVentas();
        try{
            getSQLConnection();
            cli = getClientes(ventas);
            vehi = getVehiculos(ventas);
            saveInfo(ventas, cli, vehi);
            conxOrcl.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        em.close();
        mongoC.close();
    }
    
}

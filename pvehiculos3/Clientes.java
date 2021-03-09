
package pvehiculos3;
import java.io.Serializable;

public class Clientes implements Serializable{ 
    String dni  ;		
    String nomec ;	
    int ncompras; 

    public Clientes() {
    }

    public Clientes(String dni, String nomec, Double ncompras) {
        double tempncompras = ncompras;
        this.dni = dni;
        this.nomec = nomec;
        this.ncompras = (int)tempncompras;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNomec() {
        return nomec;
    }

    public void setNomec(String nomec) {
        this.nomec = nomec;
    }

    public int getNcompras() {
        return ncompras;
    }

    public void setNcompras(int ncompras) {
        this.ncompras = ncompras;
    }

    @Override
    public String toString() {
        return "Clientes{" + "dni=" + dni + ", nomec=" + nomec + ", ncompras=" + ncompras + '}';
    }
    
}

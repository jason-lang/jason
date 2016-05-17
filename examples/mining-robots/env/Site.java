package env;

public class Site extends PlanetCell {

    int r1needed;
    int r2needed;
    int r3needed;
    
    int r1store;
    int r2store;
    int r3store;    
    
    boolean completed;  

    public Site() {
        
        r1needed = 40;
        r2needed = 40;
        r3needed = 40;
        
        r1store = 0;
        r2store = 0;
        r3store = 0;
        
        completed = false;      
    }
    
    public int getr1() {
        return r1needed;
    }

    public int getr2() {
        return r2needed;
    }
    
    public int getr3() {
        return r3needed;
    }

    public int getr1store() {
        return r1store;
    }

    public int getr2store() {
        return r2store;
    }
    
    public int getr3store() {
        return r3store;
    }
    
    public void addstore(int resource) {
        
        switch(resource) {
            case 1:             
                r1store++;
                break;
            case 2:
                r2store++;
                break;
            case 3: 
                r3store++;
                break;
        }
    }

    public int build() {
        
        int resource = 0;
        
        if(r1needed > 0 && r1store > 0) {
            r1needed--;
            r1store--;
            resource = 1;
        }
        else if(r2needed > 0 && r2store > 0) {
            r2needed--;
            r2store--;
            resource = 2;
        }
        else if(r3needed > 0 && r3store > 0) {
            r3needed--;
            r3store--;
            resource = 3;
        }
        if(r1needed == 0 && r2needed == 0 && r3needed == 0){
            completed = true;
        }
        return resource;
    }
    

    public boolean complete() {
    
        return completed;
    
    }

}

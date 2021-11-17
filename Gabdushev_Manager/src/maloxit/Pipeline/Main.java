package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.RC;

public class Main {

    public static void main(String[] args) {
        Manager manager = new Manager();
        RC rc = manager.runFromConfig(args[0]);
        if (!rc.isSuccess()) {
            System.out.println("Error: " + rc.who.get() + ": " + rc.info);
        }
    }
}
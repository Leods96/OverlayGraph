package location_iq;

import org.json.JSONArray;
import org.json.JSONObject;

public class ResponseInterpreter {

    public static void printJSONObj(JSONObject jo){
        JSONObject routes = ((JSONArray) jo.get("routes")).getJSONObject(0);
        System.out.println("routes: ");
        for(String key : routes.keySet()){
            if(key.equalsIgnoreCase("legs")){
                System.out.println("\tlegs: ");
                JSONArray legs = ((JSONArray)routes.get(key));
                for(int i=0;i < legs.length();i++){
                    JSONObject o = (JSONObject) legs.get(i);
                    for(String k : o.keySet()){
                        if(k.equalsIgnoreCase("steps")){
                            System.out.println("\t\tsteps: ");
                            JSONArray steps = ((JSONArray)o.get("steps"));
                            System.out.println("\t\t#of element in steps = " + steps.length());
                            for(int j=0;j < steps.length();j++){
                                JSONObject ob = (JSONObject) steps.get(j);
                                for(String z : ob.keySet()){
                                    System.out.println("\t\t\t"+z + " : " + ob.get(z));
                                }
                            }
                        }else if(k.equalsIgnoreCase("annotation")){
                            System.out.println("\t\tannotation: ");
                            JSONObject annotation = ((JSONObject)o.get("annotation"));
                            for(String j:annotation.keySet()) {
                                System.out.println("\t\t\t" + j + " : " + annotation.get(j));
                                if(!j.equalsIgnoreCase("metadata"))
                                    System.out.println("\t\t\t#of element in " + j + " = " + ((JSONArray)annotation.get(j)).length());
                            }
                        }else
                            System.out.println("\t\t"+k + " : " + o.get(k));
                    }
                }
            }else
                System.out.println("\t"+key + " : " + routes.get(key));
        }
        JSONArray waypoints = (JSONArray) jo.get("waypoints");
        System.out.println("waypoints: ");
        for(int i = 0; i < waypoints.length(); i++){
            JSONObject o = waypoints.getJSONObject(i);
            for(String key : o.keySet()){
                System.out.println("\t"+key + " : " + o.get(key));
            }
            System.out.println("\t------------");
        }
    }

}

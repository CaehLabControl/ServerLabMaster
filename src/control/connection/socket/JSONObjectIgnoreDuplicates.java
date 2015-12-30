/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.connection.socket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Carlos
 */
public class JSONObjectIgnoreDuplicates extends JSONObject {

     public JSONObjectIgnoreDuplicates(String json) throws JSONException {
        super(json);
    }

    JSONObjectIgnoreDuplicates(JSONObject objTemp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public JSONObject putOnce(String key, Object value) throws JSONException {
            Object storedValue;
            if (key != null && value != null) {
                if ((storedValue = this.opt(key)) != null ) {
                    if(!storedValue.equals(value))                          //Only through Exception for different values with same key
                        throw new JSONException("Duplicate key \"" + key + "\"");
                    else
                        return this;
                }
                this.put(key, value);
            }
            return this;
        }
}
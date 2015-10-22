package cz.vutbr.fit.tam.meetme.schema;

import java.util.HashMap;
import java.util.Map;

import cz.vutbr.fit.tam.meetme.R;

/**
 * @author Gabriel Lehocky
 */
public class GroupColor {

    // <color resource id, number of usage>
    private HashMap<Integer, Integer> colors = new HashMap<>();

    public GroupColor(){
        colors.put(R.color.flat_1_light, 0);
        colors.put(R.color.flat_2_light, 0);
        colors.put(R.color.flat_3_light, 0);
        colors.put(R.color.flat_4_light, 0);
        colors.put(R.color.flat_5_light, 0);
        colors.put(R.color.flat_6_light, 0);
    }

    public Integer getNextColor(){

        Integer colorResource = R.color.flat_1_light;
        Integer useNumber = colors.get(colorResource);

        for (Map.Entry<Integer, Integer> entry : colors.entrySet()) {
            if (entry.getValue() < useNumber){
                colorResource = entry.getKey();
                useNumber = entry.getValue();
            }
        }

        colors.put(colorResource, useNumber +1);

        return colorResource;
    }

}

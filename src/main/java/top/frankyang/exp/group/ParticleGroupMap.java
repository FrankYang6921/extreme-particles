package top.frankyang.exp.group;

import java.util.HashMap;

public class ParticleGroupMap extends HashMap<String, ParticleGroup> {
    public ParticleGroupMap() {
        super();
        put(null, new ParticleGroup());
    }
}

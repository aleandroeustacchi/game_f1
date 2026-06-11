package it.unicam.cs.mpgc.rpg130017.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.unicam.cs.mpgc.rpg130017.model.GameState;

import java.io.File;
import java.io.IOException;

public class JsonSaveRepository implements SaveRepository {
    private final ObjectMapper objectMapper;

    public JsonSaveRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void save(GameState state, String filename) throws IOException {
        File file = new File(filename);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        objectMapper.writeValue(file, state);
    }

    @Override
    public GameState load(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new IOException("Save file " + filename + " does not exist.");
        }
        return objectMapper.readValue(file, GameState.class);
    }
}

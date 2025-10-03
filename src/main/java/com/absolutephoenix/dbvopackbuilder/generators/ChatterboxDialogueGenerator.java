package com.absolutephoenix.dbvopackbuilder.generators;

import com.absolutephoenix.dbvopackbuilder.utils.ElevenLabsFileHandling;
import com.absolutephoenix.dbvopackbuilder.utils.LogHelper;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;

public class ChatterboxDialogueGenerator implements DialogueGenerator{
    public String endpoint;
    public Path ref_file;
    // TODO: more dials
    public double exaggeration;
    public double cfg_weight;
    public double temperature;

    public ChatterboxDialogueGenerator(String endpoint, Path ref_file, double exaggeration, double cfg_weight, double temperature){
        this.endpoint = endpoint;
        this.exaggeration = exaggeration;
        this.cfg_weight = cfg_weight;
        this.temperature = temperature;
        this.ref_file = ref_file;
    }

    @Override
    public boolean generateDialogue(String dialogue, String outFileName) {
        Path wavePath = Paths.get("staging/wav/", outFileName + ".wav");
        if(Files.exists(wavePath)){
            LogHelper.yellowInfo("SKIPPING: " + dialogue + " (file already exists)");
            return true;
        }
        String[] components = this.endpoint.split(":");
        if(components.length != 2){
            LogHelper.error("Invalid endpoint. Must be in the form 'hostname:port'");
        }
        String hostname = components[0];
        int port = -1;
        try{
             port = Integer.parseInt(components[1]);
        }catch(NumberFormatException e){
            LogHelper.error("Invalid port format. It must be an integer.");
        }
        try(Socket socket = new Socket(hostname, port)){
            InputStream is = socket.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );
            OutputStream os = socket.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, StandardCharsets.UTF_8)
            );

            if(dialogue.contains("\n") || dialogue.contains("\r")){
                LogHelper.error("Dialogue contains newline or carriage return; this breaks serialization.");
                LogHelper.error("Offending Dialogue: " + dialogue);
                return false;
            }

            String refFileName = this.ref_file.getFileName().toString();

            writer.write(
                    String.join("|",
                        Arrays.asList(refFileName, this.exaggeration+"", this.cfg_weight+"", this.temperature+"")
                    ) + "\n"
            );
            writer.flush();
            String response = reader.readLine();
            if(response.strip().equals("SEND_REF")){
                LogHelper.info("sending ref file on request");
                byte[] ref_audio = new FileInputStream(this.ref_file.toFile()).readAllBytes();
                writer.write(ref_audio.length + "\n");
                writer.flush();
                os.write(ref_audio);
                os.flush();
                LogHelper.info("sent ref file on request");
            }
            writer.write(dialogue + "\n");
            writer.flush();
            byte[] generated_audio = is.readAllBytes();
            Files.createDirectories(Path.of("staging/wav/"));
            Files.write(wavePath, generated_audio, StandardOpenOption.CREATE);
            return true;
        }catch (IOException e){
            LogHelper.error("IO error encountered while generating dialogue");
            LogHelper.error(e.toString());
            return false;
        }

        // TODO: this
        // spawn a python process
        // python process does the gradio endpoint call
        // returns out file path or error message
        // move this file to requested out file location. Convert to wav if needed.

        // ideally, this whole process should be done in java and not need to spawn a python process
        // IDEA: Use jython
    }
}

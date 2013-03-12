package org.upd;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpRequest;
import org.prop.Properties;
import org.prop.Section;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * todo: absolutely disgusting
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * oldrsclient
 * 10.3.2013
 */
public class Updater {

    private String playersName = null, localPlayerName = null, playerClassName = null, entityClassName = null
            , entityXYName[] = new String[2], areaXYName[] = new String[2], chatMessageTypes = null
            , chatMessageSenders = null, chatMessages = null, playerName = null;
    private ObjectType playersType = null, localPlayerType = null, entityXYType[] = new ObjectType[2]
            , areaXYType[] = new ObjectType[2];
    private int entityXYMultiplier[] = new int[2], areaXYMultiplier[] = new int[2];

    private void downloadFile(URL url, String outFilename) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpHost(url.getHost()), new BasicHttpRequest("HEAD", url.getFile()));
        long filesize = Long.parseLong(response.getLastHeader("Content-length").getValue());
        ReadableByteChannel inchannel = Channels.newChannel(url.openStream());
        FileOutputStream out = new FileOutputStream(outFilename);
        out.getChannel().transferFrom(inchannel, 0, filesize);
        out.getChannel().close();
        out.close();
    }

    public void update() throws URISyntaxException, IOException {
        Properties props = new Properties();
        props.load("oldrsclient.properties");

        System.out.println("Downloading gamepack");
        downloadFile(new URL(props.getSection("launcher").getProperty("base_url") + "/gamepack.jar"), "gamepack.jar.temp");
        List<String> classes = new ArrayList<String>();
        JarFile jar = new JarFile("gamepack.jar.temp");
        Enumeration<JarEntry> entries = jar.entries();
        while(entries.hasMoreElements()) {
            JarEntry next = entries.nextElement();
            if(next.getName().endsWith(".class"))
                classes.add(next.getName());
        }
        jar.close();
        System.out.println("Searching for field references");
        for(String s : classes) {// IT'S BECAUSE WE NEED THESE VALUES BEFORE GOING TO THE NEXT MAGICAL PART
            try {
                ClassGen cg = new ClassGen(new ClassParser(jar.getName(), s).parse());
                for(Method m : cg.getMethods()) {
                    MethodGen mg = new MethodGen(m, cg.getClassName(), cg.getConstantPool());
                    if(mg.isAbstract() || mg.isInterface() || mg.isEnum() || mg.getInstructionList().isEmpty())
                        continue;
                    Iterator<InstructionHandle[]> it = new InstructionFinder(mg.getInstructionList())
                            .search("GETSTATIC ConstantPushInstruction NEW StackInstruction INVOKESPECIAL StackInstruction AASTORE PUTSTATIC");
                    if(it.hasNext()) {
                        InstructionHandle match[] = it.next();
                        GETSTATIC g = (GETSTATIC) match[0].getInstruction();
                        PUTSTATIC p = (PUTSTATIC) match[7].getInstruction();
                        NEW n = (NEW) match[2].getInstruction();
                        playersName = g.getFieldName(cg.getConstantPool());
                        playersType = g.getLoadClassType(cg.getConstantPool());
                        localPlayerName = p.getFieldName(cg.getConstantPool());
                        localPlayerType = p.getLoadClassType(cg.getConstantPool());
                        playerClassName = n.getLoadClassType(cg.getConstantPool()).getClassName();
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        for(String s : classes) {// HERE IS THE NEXT MAGICAL PART
            try {
                ClassGen cg = new ClassGen(new ClassParser(jar.getName(), s).parse());
                if(cg.getClassName().equals(playerClassName))
                    entityClassName = cg.getSuperclassName();// :)
                find(cg);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        Section cs = new Section("fields");
        cs.putProperty("players", playersType.getClassName() + "." + playersName);
        cs.putProperty("player_name", playerName);
        cs.putProperty("local_player", localPlayerType.getClassName() + "." + localPlayerName);
        cs.putProperty("player_class", playerClassName);
        cs.putProperty("entity_class", entityClassName);
        cs.putProperty("entity_x", entityXYName[0]);
        cs.putProperty("entity_x_multiplier", String.valueOf(entityXYMultiplier[0]));
        cs.putProperty("entity_y", entityXYName[1]);
        cs.putProperty("entity_y_multiplier", String.valueOf(entityXYMultiplier[1]));
        cs.putProperty("area_x", areaXYType[0].getClassName() + "." + areaXYName[0]);
        cs.putProperty("area_x_multiplier", String.valueOf(areaXYMultiplier[0]));
        cs.putProperty("area_y", areaXYType[1].getClassName() + "." + areaXYName[1]);
        cs.putProperty("area_y_multiplier", String.valueOf(areaXYMultiplier[1]));
        cs.putProperty("chat_messages", chatMessages);
        cs.putProperty("chat_message_types", chatMessageTypes);
        cs.putProperty("chat_message_senders", chatMessageSenders);

        System.out.println("Saving");

        props.putSection(cs, true);
        props.save("oldrsclient.properties");

        ParamParser p = new ParamParser("oldrsclient.properties");
        p.parseAndSave();

        new File("gamepack.jar.temp").renameTo(new File("gamepack.jar"));

        System.out.println("Done");
    }

    private void find(ClassGen cg) {
        if(cg.getClassName().equals(playerClassName)) {
            for(Field f : cg.getFields()) {
                FieldGen fg = new FieldGen(f, cg.getConstantPool());
                if(fg.getType().equals(Type.STRING)) {
                    playerName = fg.getName();
                }
            }
        }
        for(Method m : cg.getMethods()) {
            MethodGen mg = new MethodGen(m, cg.getClassName(), cg.getConstantPool());
            if(mg.isAbstract() || mg.isInterface() || mg.isEnum() || mg.getInstructionList().isEmpty())
                continue;
            Type argtypes[] = mg.getArgumentTypes();// very vVERVVERY SPECIFIC 1!!1121
            if(argtypes.length < 4 || !argtypes[0].equals(Type.INT) || !argtypes[1].equals(Type.STRING) || !argtypes[2].equals(Type.STRING) || !argtypes[3].equals(Type.STRING))
                continue;
            Iterator<InstructionHandle[]> it = new InstructionFinder(mg.getInstructionList())
                    .search("GETSTATIC ICONST (ILOAD|ALOAD)");
            while(it.hasNext()) {
                InstructionHandle match[] = it.next();
                ICONST iconst = (ICONST) match[1].getInstruction();
                if(iconst.getValue().intValue() != 0)
                    continue;
                LocalVariableInstruction load = (LocalVariableInstruction) match[2].getInstruction();
                GETSTATIC get = (GETSTATIC) match[0].getInstruction();
                if(load.getIndex() == 0 && chatMessageTypes == null)
                    chatMessageTypes = get.getLoadClassType(cg.getConstantPool()) + "." + get.getFieldName(cg.getConstantPool());
                else if(load.getIndex() == 1 && chatMessageSenders == null)
                    chatMessageSenders = get.getLoadClassType(cg.getConstantPool()) + "." + get.getFieldName(cg.getConstantPool());
                else if(load.getIndex() == 2 && chatMessages == null)
                    chatMessages = get.getLoadClassType(cg.getConstantPool()) + "." + get.getFieldName(cg.getConstantPool());
            }
        }
        for(Method m : cg.getMethods()) {// yep yep yep
            MethodGen mg = new MethodGen(m, cg.getClassName(), cg.getConstantPool());
            if(mg.isAbstract() || mg.isInterface() || mg.isEnum() || mg.getInstructionList().isEmpty())
                continue;
            Iterator<InstructionHandle[]> it = new InstructionFinder(mg.getInstructionList())
                    // BIPUSH IfInstruction (LDC|GETSTATIC) (GETSTATIC|GETFIELD) (LDC|GETFIELD) IMUL BIPUSH ISHR (LDC|GETSTATIC) (LDC|GETSTATIC)
                    .search("(BIPUSH|ILOAD) (BIPUSH|ILOAD) IfInstruction Instruction Instruction Instruction IMUL BIPUSH ISHR Instruction Instruction IMUL IADD");
            while(it.hasNext()) {
                InstructionHandle match[] = it.next();
                int entmulti = 0;
                GETSTATIC pget = null;
                GETFIELD entfield = null;
                for(int i = 3; i < 6; i++) {
                    Instruction inst = match[i].getInstruction();
                    if(inst instanceof LDC)
                        entmulti = (Integer) ((LDC) inst).getValue(cg.getConstantPool());
                    else if(inst instanceof GETSTATIC)
                        pget = (GETSTATIC) inst;
                    else if(inst instanceof GETFIELD)
                        entfield = (GETFIELD) inst;
                }
                int areamulti = 0;
                GETSTATIC areaget = null;
                for(int i = 9; i < 11; i++) {
                    Instruction inst = match[i].getInstruction();
                    if(inst instanceof LDC)
                        areamulti = (Integer) ((LDC) inst).getValue(cg.getConstantPool());
                    else if(inst instanceof GETSTATIC)
                        areaget = (GETSTATIC) inst;
                }
                int val;
                if(match[0].getInstruction() instanceof BIPUSH)
                    val = ((BIPUSH) match[0].getInstruction()).getValue().intValue();
                else
                    val = ((BIPUSH) match[1].getInstruction()).getValue().intValue();
                if((entmulti == 0 || pget == null || entfield == null || areamulti == 0 || areaget == null)
                        || !pget.getFieldName(cg.getConstantPool()).equals(localPlayerName)
                        || !pget.getLoadClassType(cg.getConstantPool()).equals(localPlayerType))
                    continue;
                int idx = val == 18 ? 0 : 1;
                entityXYName[idx] = entfield.getFieldName(cg.getConstantPool());
                entityXYType[idx] = entfield.getLoadClassType(cg.getConstantPool());
                entityXYMultiplier[idx] = entmulti;
                areaXYName[idx] = areaget.getFieldName(cg.getConstantPool());
                areaXYType[idx] = areaget.getLoadClassType(cg.getConstantPool());
                areaXYMultiplier[idx] = areamulti;
            }
            it = new InstructionFinder(mg.getInstructionList())
                    // BIPUSH IfInstruction (LDC|GETSTATIC) (LDC|GETSTATIC) IMUL (LDC|GETSTATIC) (GETSTATIC|GETFIELD) (LDC|GETFIELD)
                    .search("(BIPUSH|ILOAD) (BIPUSH|ILOAD) IfInstruction Instruction Instruction IMUL Instruction Instruction Instruction IMUL BIPUSH ISHR IADD");
            while(it.hasNext()) {
                InstructionHandle match[] = it.next();
                int areamulti = 0;
                GETSTATIC areaget = null;
                for(int i = 3; i < 5; i++) {
                    Instruction inst = match[i].getInstruction();
                    if(inst instanceof LDC)
                        areamulti = (Integer) ((LDC) inst).getValue(cg.getConstantPool());
                    else if(inst instanceof GETSTATIC)
                        areaget = (GETSTATIC) inst;
                }
                int entmulti = 0;
                GETSTATIC pget = null;
                GETFIELD entfield = null;
                for(int i = 6; i < 9; i++) {
                    Instruction inst = match[i].getInstruction();
                    if(inst instanceof LDC)
                        entmulti = (Integer) ((LDC) inst).getValue(cg.getConstantPool());
                    else if(inst instanceof GETSTATIC)
                        pget = (GETSTATIC) inst;
                    else if(inst instanceof GETFIELD)
                        entfield = (GETFIELD) inst;
                }
                int val;
                if(match[0].getInstruction() instanceof BIPUSH)
                    val = ((BIPUSH) match[0].getInstruction()).getValue().intValue();
                else
                    val = ((BIPUSH) match[1].getInstruction()).getValue().intValue();
                if((entmulti == 0 || pget == null || entfield == null || areamulti == 0 || areaget == null)
                        || !pget.getFieldName(cg.getConstantPool()).equals(localPlayerName)
                        || !pget.getLoadClassType(cg.getConstantPool()).equals(localPlayerType))
                    continue;
                int idx = val == 18 ? 0 : 1;
                entityXYName[idx] = entfield.getFieldName(cg.getConstantPool());
                entityXYType[idx] = entfield.getLoadClassType(cg.getConstantPool());
                entityXYMultiplier[idx] = entmulti;
                areaXYName[idx] = areaget.getFieldName(cg.getConstantPool());
                areaXYType[idx] = areaget.getLoadClassType(cg.getConstantPool());
                areaXYMultiplier[idx] = areamulti;
            }
            // and finally(?)
            it = new InstructionFinder(mg.getInstructionList())
                    // BIPUSH IfInstruction (LDC|GETSTATIC) (LDC|GETSTATIC) IMUL (LDC|GETSTATIC) (GETSTATIC|GETFIELD) (LDC|GETFIELD)
                    .search("(BIPUSH|ILOAD) (BIPUSH|ILOAD) IfInstruction (BIPUSH|ILOAD) (BIPUSH|ILOAD) IfInstruction NEW StackInstruction INVOKESPECIAL ATHROW Instruction Instruction IMUL Instruction Instruction Instruction IMUL BIPUSH ISHR IADD");
            while(it.hasNext()) {
                InstructionHandle match[] = it.next();
                int areamulti = 0;
                GETSTATIC areaget = null;
                for(int i = 10; i < 12; i++) {
                    Instruction inst = match[i].getInstruction();
                    if(inst instanceof LDC)
                        areamulti = (Integer) ((LDC) inst).getValue(cg.getConstantPool());
                    else if(inst instanceof GETSTATIC)
                        areaget = (GETSTATIC) inst;
                }
                int entmulti = 0;
                GETSTATIC pget = null;
                GETFIELD entfield = null;
                for(int i = 13; i < 16; i++) {
                    Instruction inst = match[i].getInstruction();
                    if(inst instanceof LDC)
                        entmulti = (Integer) ((LDC) inst).getValue(cg.getConstantPool());
                    else if(inst instanceof GETSTATIC)
                        pget = (GETSTATIC) inst;
                    else if(inst instanceof GETFIELD)
                        entfield = (GETFIELD) inst;
                }
                int val;
                if(match[0].getInstruction() instanceof BIPUSH)
                    val = ((BIPUSH) match[0].getInstruction()).getValue().intValue();
                else
                    val = ((BIPUSH) match[1].getInstruction()).getValue().intValue();
                if((entmulti == 0 || pget == null || entfield == null || areamulti == 0 || areaget == null)
                        || !pget.getFieldName(cg.getConstantPool()).equals(localPlayerName)
                        || !pget.getLoadClassType(cg.getConstantPool()).equals(localPlayerType))
                    continue;
                int idx = val == 18 ? 0 : 1;
                entityXYName[idx] = entfield.getFieldName(cg.getConstantPool());
                entityXYType[idx] = entfield.getLoadClassType(cg.getConstantPool());
                entityXYMultiplier[idx] = entmulti;
                areaXYName[idx] = areaget.getFieldName(cg.getConstantPool());
                areaXYType[idx] = areaget.getLoadClassType(cg.getConstantPool());
                areaXYMultiplier[idx] = areamulti;
            }
        }
    }
}

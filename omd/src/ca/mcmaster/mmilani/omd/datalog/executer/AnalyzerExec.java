package ca.mcmaster.mmilani.omd.datalog.executer;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AnalyzerExec {
    public static void main(String[] args) throws IOException {
        if (checkOption(args, "-i")) {
            integrateResults(args[0]);
            return;
        }
        String inputDirPath = args[0];
        File inDir = new File(inputDirPath);
        File[] list = inDir.listFiles((dir, name) -> name.endsWith(".txt"));
        for (File file : list) {
            try {
                ArrayList<String> options = new ArrayList<>();
                options.add(file.getAbsolutePath());
                options.addAll(Arrays.asList(args).subList(1, args.length));
                options.add("-d");
                options.add("smalldb");
                options.add("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\src\\db-config.properties");
                exec(OntologyAnalyzer.class, options);
            } catch (Exception e) {
                System.out.println(file.getName() + " return with error!");
                e.printStackTrace();
            }
        }
    }

    private static void integrateResults(String path) throws IOException {
        File[] files = new File(path).listFiles((dir, name) -> name.endsWith(".res"));
        Set<Map<String, Object>> results = new HashSet<>();
        List<String> keys = new ArrayList<>(); keys.add("name");keys.add("t_vlog");
        for (File file : files) {
            System.out.println("Processing " + file.getName());
            Map<String, Object> result = new HashMap<>();
            BufferedReader in = new BufferedReader(new FileReader(file));
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            result.put("name", "ontology-" + name);
            String line = in.readLine().replaceAll(" ", "");
            while (line != null) {
                String key = line.substring(0, line.indexOf(":"));
                String value = line.substring(line.indexOf(":")+1, line.length());
                result.put(key,value);
                if (!keys.contains(key)) keys.add(key);
                line = in.readLine();
            }
            File vlog = new File(path + "\\" + name + ".vlog");
            if (vlog.exists()) {
                BufferedReader vlogIn = new BufferedReader(new FileReader(vlog));
                String content = vlogIn.readLine();
                if (content == null)
                    result.put("t_vlog", "-1");
                else
                    result.put("t_vlog", Float.parseFloat(content) + "");
            } else {
                result.put("t_vlog", "-1");
            }
            if (!result.keySet().contains(OntologyAnalyzer.TIME_TERMINATES_CHASE)) {
                result.put(OntologyAnalyzer.TIME_TERMINATES_CHASE, "-1");
                result.put(OntologyAnalyzer.TERMINATES_CHASE, "true");
            }
            results.add(result);
        }
        String header = "";
        keys.sort((o1, o2) -> {
            if (o1.equals(o2)) return 0;
            if (o1.equals("name")) return -1;
            if (o2.equals("name")) return 1;
            return o1.compareTo(o2);
        });
        for (String key : keys) {
            header += key + ",";
        }
        String fileName = path + "\\results.csv";
        File file = new File(fileName); file.createNewFile();
        FileWriter out = new FileWriter(file);
        out.write(header.substring(0, header.length()-1) + "\n");

        for (Object o : results) {
            Map<String, Object> result = (Map<String, Object>) o;
            String line = "";
            for (String key : keys) {
                System.out.println("key = " + key);
                String value = (String) result.get(key);
                line += value + ",";
            }
            out.write(line.substring(0, line.length()-1) + "\n");
        }
        out.close();
    }

    public static boolean checkOption(String[] args, String option) {
        for (String arg : args) {
            if (arg.equals(option)) return true;
        }
        return false;
    }

    public static boolean exec(Class<OntologyAnalyzer> klass, List<String> options) throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
        int mbRam = (int) (memorySize / 1024 / 1024);
        int vbRam = (int) (mbRam * 0.85);
        String className = klass.getCanonicalName();
        String vmRamParams = "-Xmx" + vbRam + "m";
        String stacksize = "-Xss2m";
        String extraParams = "-Djava.util.logging.config.class=it.unibas.lunatic.utility.JavaUtilLoggingConfig";
        List<String> commands = new ArrayList<String>();
        commands.add(javaBin);
        commands.add("-cp");
        commands.add(classpath);
        commands.add(vmRamParams);
        commands.add(stacksize);
        commands.add(extraParams);
        commands.add(className);
        commands.addAll(options);
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = builder.start();
        process.waitFor(5, TimeUnit.MINUTES);
        int exitValue = 0;
        try {
            exitValue = process.exitValue();
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
            System.out.println("Process terminates!");
            process.destroy();
        }
        return (exitValue != 0); //Return true if errors
    }

    public static String getOptionValue(String[] args, String option, int valueIndex) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals(option)) {
                return args[i + valueIndex];
            }
        }
        return null;
    }
}

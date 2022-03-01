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
        List files = getFiles(inputDirPath);
        for (int i = 0; i < 1; i++) {
        for (Object next : files) {
            File file = (File) next;
            try {
                ArrayList<String> options = new ArrayList<>();
                options.add(file.getAbsolutePath());
                options.addAll(Arrays.asList(args).subList(1, args.length));
//                options.add("-d");
//                options.add("-a");
//                options.add("-r");
//                options.add("smalldb");
//                options.add("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\src\\db-config.properties");
                exec(LinearOntologyAnalyzer.class, options);
            } catch (Exception e) {
                System.out.println(file.getName() + " return with error!");
                e.printStackTrace();
            }
        }}
    }

    private static void integrateResults(String path) throws IOException {
        List<File> files = new ArrayList<>();
        File rootDir = new File(path);
        files.addAll(List.of(rootDir.listFiles((dir, name) -> name.endsWith(".res"))));
        File[] subdirs = rootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (File subdir : subdirs) {
            files.addAll(List.of(subdir.listFiles((dir, name) -> name.endsWith(".res"))));
        }

        Set<Map<String, Object>> results = new HashSet<>();
        List<String> keys = new ArrayList<>(); keys.add("name");keys.add("t_vlog");
        for (File file : files) {
            System.out.println("Processing " + file.getName());
            Map<String, Object> result = new HashMap<>();
            BufferedReader in = new BufferedReader(new FileReader(file));
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            String ontologyname = file.getAbsolutePath().substring(path.length());
//            result.put("name", "ontology-" + name);
            result.put("name", ontologyname);
            String line = in.readLine().replaceAll(" ", "");
            List<Double> timesD = new ArrayList<>();
            while (line != null) {
                String key = line.substring(0, line.indexOf(":"));
                String value = line.substring(line.indexOf(":")+1);
                if (key.equals(OntologyAnalyzer.TIME_TERMINATES_GRAPH)) {
                    timesD.add(Double.parseDouble(value));
                } else {
                    result.put(key,value);
                    if (!keys.contains(key)) keys.add(key);
                }
                line = in.readLine();
            }
            double avg_t = average(timesD), std_t = std(timesD);
            String key = OntologyAnalyzer.TIME_TERMINATES_GRAPH + "_avg";
            result.put(key,avg_t);
            if (!keys.contains(key)) keys.add(key);
            key = OntologyAnalyzer.TIME_TERMINATES_GRAPH + "_std";
            result.put(key,std_t);
            if (!keys.contains(key)) keys.add(key);
            File vlog = new File(file.getParentFile().getAbsolutePath() + "/" + name + ".vlog");
            /*if (vlog.exists()) {
                BufferedReader vlogIn = new BufferedReader(new FileReader(vlog));
                String content = vlogIn.readLine();
                List<Double> times = new ArrayList<>();
                while(content != null) {
                    times.add(Double.parseDouble(content)); vlogIn.readLine();
                    content = vlogIn.readLine();
                }
                key = "t_vlog_avg";
                result.put(key, average(times) + "");
                if (!keys.contains(key)) keys.add(key);
                key = "t_vlog_std";
                result.put(key, std(times) + "");
                if (!keys.contains(key)) keys.add(key);
            } else {
                result.put("t_vlog", "-1");
            }*/
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
        String fileName = path + "/results.csv";
        File file = new File(fileName); file.createNewFile();
        FileWriter out = new FileWriter(file);
        out.write(header.substring(0, header.length()-1) + "\n");

        for (Object o : results) {
            Map<String, Object> result = (Map<String, Object>) o;
            String line = "";
            for (String key : keys) {
                System.out.println("key = " + key);
                String value = result.get(key) +"";
                line += value + ",";
            }
            out.write(line.substring(0, line.length()-1) + "\n");
        }
        out.close();
    }

    private static double std(List<Double> values) {
        double mean = average(values);
        double temp = 0;

        for (int i = 0; i < values.size(); i++)
        {
            double val = values.get(i);

            // Step 2:
            double squrDiffToMean = Math.pow(val - mean, 2);

            // Step 3:
            temp += squrDiffToMean;
        }

        // Step 4:
        double meanOfDiffs = (double) temp / (double) (values.size());

        // Step 5:
        return Math.sqrt(meanOfDiffs);
    }

    private static double average(List<Double> values) {
        double sum = 0;
        for (Double value : values) {
            sum += value;     
        }
        return sum/values.size();
    }

    public static boolean checkOption(String[] args, String option) {
        for (String arg : args) {
            if (arg.equals(option)) return true;
        }
        return false;
    }

    public static boolean exec(Class klass, List<String> options) throws Exception {
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

    public static List getFiles(String inputDirPath) {
        List dirs = new ArrayList();
        File dir = new File(inputDirPath);
        dirs.add(dir);

        File[] subdirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (File subdir : subdirs) {
            dirs.add(subdir);
        }

        List files = new ArrayList();
        for (Object o : dirs) {
            File tempDir = (File) o;
            File[] tempFiles = tempDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    String suffix = ".txt";
                    return file.getName().endsWith(suffix);
                }
            });
            System.out.println("tempFiles.length = " + tempFiles.length);
            for (File tempFile : tempFiles) {
                files.add(tempFile);
            }
        }
        return files;
    }
}

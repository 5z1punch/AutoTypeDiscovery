package com.xlab.atd;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AutoTypeDiscovery {
    private boolean cacheLoaded = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoTypeDiscovery.class);
    private final HashSet<String> autotypeClasses = new HashSet<>(Arrays.asList(initAutoTypeList));
    private final HashMap<String, List<Class>> cacheAllAssignMap = new HashMap<>();
    private final List<String> hasDiscovered = new ArrayList<>();
    private final HashMap<String, Pair<Constructor<?>,String[]>> cacheCreatorConstructor = new HashMap<>();
    public List<Class> allClasses;
    private final HashSet<String> cantDeserialize = new HashSet<>(Arrays.asList(
            // 一些因为很蛋疼的原因无法加载的类
            "org.apache.http.impl.bootstrap.WorkerPoolExecutor"
    ));
    private static final String[] initAutoTypeList = {
            "java.lang.AutoCloseable",
//            "java.util.BitSet",
//            "org.springframework.cache.support.NullValue",
//            "org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken",
//            "org.springframework.security.oauth2.common.DefaultOAuth2AccessToken",
//            "org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken",
//            "org.springframework.remoting.support.RemoteInvocation",
//            "org.springframework.remoting.support.RemoteInvocationResult",
//            "org.springframework.security.web.savedrequest.DefaultSavedRequest",
//            "org.springframework.security.web.savedrequest.SavedCookie",
//            "org.springframework.security.web.csrf.DefaultCsrfToken",
//            "org.springframework.security.web.authentication.WebAuthenticationDetails",
//            "org.springframework.security.core.context.SecurityContextImpl",
//            "org.springframework.security.authentication.UsernamePasswordAuthenticationToken",
//            "org.springframework.security.core.authority.SimpleGrantedAuthority",
//            "org.springframework.security.core.userdetails.User",
            // Test ⬇️
//            "javax.swing.JEditorPane",
    };
    private static final List<String> jsonParserClasses = new ArrayList<String>(Arrays.asList(
            "java.util.Collection",
            "java.util.Set",
            "java.util.Map",
            "java.util.List",
            "java.lang.Throwable"
    ));
    private static final List<String> banParentClasses = new ArrayList<String>(Arrays.asList(
            "java.lang.ClassLoader",
            "javax.sql.RowSet",
            "javax.sql.DataSource"
    ));
    private static final List<String> alreadyLoadedClasses = new ArrayList<String>(Arrays.asList(
            "java.text.SimpleDateFormat",
            "java.sql.Timestamp",
            "java.sql.Date",
            "java.sql.Time",
            "java.util.Date",
            "java.util.Calendar",
            "javax.xml.datatype.XMLGregorianCalendar",
            "java.lang.Object",
            "java.lang.String",
            "java.lang.StringBuffer",
            "java.lang.StringBuilder",
            "java.lang.Character",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Integer",
            "java.lang.Long",
            "java.math.BigInteger",
            "java.math.BigDecimal",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Boolean",
            "java.lang.Class",
            "java.util.concurrent.atomic.AtomicBoolean",
            "java.util.concurrent.atomic.AtomicInteger",
            "java.util.concurrent.atomic.AtomicLong",
            "java.util.concurrent.atomic.AtomicReference",
            "java.lang.ref.WeakReference",
            "java.lang.ref.SoftReference",
            "java.util.UUID",
            "java.util.TimeZone",
            "java.util.Locale",
            "java.util.Currency",
            "java.net.Inet4Address",
            "java.net.Inet6Address",
            "java.net.InetSocketAddress",
            "java.io.File",
            "java.net.URI",
            "java.net.URL",
            "java.util.regex.Pattern",
            "java.nio.charset.Charset",
            "com.alibaba.fastjson.JSONPath",
            "java.lang.Number",
            "java.util.concurrent.atomic.AtomicIntegerArray",
            "java.util.concurrent.atomic.AtomicLongArray",
            "java.lang.StackTraceElement",
            "java.io.Serializable",
            "java.lang.Cloneable",
            "java.lang.Comparable",
            "java.io.Closeable",
            "com.alibaba.fastjson.JSONPObject",
            "java.awt.Rectangle",
            "java.awt.Point",
            "java.awt.Font",
            "java.awt.Color"
    ));
    static class AutoTypeGraphUtils{
        static String output = "cytoscape";
        static String dataFile = "data.js";
        static String template = "window.element_data =\n";
        static List<HashMap<String, String>> nodes = new ArrayList();
        static HashSet<String> autotypeClasses = new HashSet<>();
        static List<HashMap<String, String>> edges = new ArrayList();
        static List<HashMap<String, String>> scanedNodes = new ArrayList();
        static List<HashMap<String, String>> realEdges = new ArrayList();
        static HashMap<String, List<HashMap>> edgesNodeMap = new HashMap<>();
        static int scanedNodesIndex = 0;
        static int realEdgesIndex = 0;
        static String scanedNodesStr = "";
        static String realEdgesStr = "";
        static boolean drawFrame = false;
        static int frameNo = 1;
        public static void addNode(Class clazz){
            System.out.println(clazz.getName());
            HashMap<String, String> node = new HashMap<>();
            node.put("id", clazz.getName());
            String type;
            if(clazz.isInterface()){
                type = "Interface";
            }
            else if(clazz.isEnum()){
                type = "Enum";
            }
            else if(clazz.isMemberClass()){
                type = "MemClass";
            }
            else if(Modifier.isAbstract(clazz.getModifiers())){
                type = "AbsClass";
            }
            else{
                type = "Class";
            }
            node.put("type", type);
            nodes.add(node);
            autotypeClasses.add(clazz.getName());
            if(drawFrame){
                saveFrame();
            }
        }
        public static void addEdge(String source, String target, String label){
            for(HashMap<String, String> edge: edges){
                if(source.equals(edge.get("source"))&& target.equals(edge.get("target"))){
                    return;
                }
            }

            HashMap<String, String> edge = new HashMap();
            edge.put("source", source);
            edge.put("target", target);
            edge.put("label", label);
            edges.add(edge);
            String[] keys = new String[] {source, target};
            for(String key : keys ){
                List<HashMap> _edges = edgesNodeMap.getOrDefault(key, null);
                if(_edges==null){
                    _edges = new ArrayList<>();
                    _edges.add(edge);
                    edgesNodeMap.put(key, _edges);
                }
                else{
                    _edges.add(edge);
                }
            }
        }
        public static void initNode(String[] initClasses){
            System.out.println("[+] init nodes");
            for(String clazz: initClasses){
                System.out.println(clazz);
                HashMap<String, String> node = new HashMap<>();
                node.put("id", clazz);
                node.put("type", "Source");
                scanedNodes.add(node);
                autotypeClasses.add(clazz);
            }
        }
        public static void save(){
            save(output+ File.separator+dataFile);
        }
        public static void saveFrame(){
            String outputFile = output+File.separator+"frame/data."+frameNo+".js";
            save(outputFile);
            frameNo++;
        }
        public static void save(String outputFile){
            for(HashMap<String, String> _node: nodes){
                String nodeId = _node.get("id");
                List<HashMap> _edges = edgesNodeMap.get(nodeId);
                for(HashMap<String, String> edge: _edges){
                    boolean sourceExist = edge.get("source").equals(nodeId);
                    boolean targetExist = edge.get("target").equals(nodeId);
                    if(realEdges.contains(edge))
                        continue;
                    if(!sourceExist && autotypeClasses.contains(edge.get("source")))
                        sourceExist = true;
                    if(!targetExist && autotypeClasses.contains(edge.get("target")))
                        targetExist = true;
                    if(sourceExist && targetExist) {
                        realEdges.add(edge);
                    }
                }
            }
            scanedNodes.addAll(nodes);
            nodes = new ArrayList();

            String data = template+"\n{\n\tnodes:[\n";
            for(;scanedNodesIndex<scanedNodes.size();scanedNodesIndex++){
                HashMap<String, String> node = scanedNodes.get(scanedNodesIndex);
                scanedNodesStr += String.format("{data:{id:'%s',type:'%s'}},\n",node.get("id"), node.get("type"));
            }
            data += scanedNodesStr;
            data += "],\n\tedges:[\n";
            for(;realEdgesIndex<realEdges.size();realEdgesIndex++){
                HashMap<String, String> edge = realEdges.get(realEdgesIndex);
                realEdgesStr += String.format("{data:{source:'%s',target:'%s',label:'%s'}},\n",edge.get("source"), edge.get("target"), edge.get("label"));
            }
            data += realEdgesStr;
            data += "]\n\t}\n";
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
                out.write(data);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private final List<Class> cantExpect = new ArrayList<>(Arrays.asList(
            Serializable.class,
            Cloneable.class,
            Closeable.class,
            EventListener.class,
            Iterable.class,
            Collection.class
    ));

    public AutoTypeDiscovery(ReflectClassEnumerator classResourceEnumerator) {
        try {
            this.allClasses = classResourceEnumerator.getAllClasses();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        AutoTypeGraphUtils.initNode(initAutoTypeList);
        try {
            if(this.cacheLoaded){
                return;
            }
            for(String initClass: initAutoTypeList){
                for(Class clazz: this.allClasses){
                    if(!clazz.getName().equals(initClass)){
                        continue;
                    }
                    try{
                        mergeToAutotypeClasses(discoverFromClass(clazz));
                    }
                    catch (NoClassDefFoundError | TypeNotPresentException ncdfe){
                        LOGGER.error("Autotype init class loaded failed: "+clazz.getName()
                                +",need dependencies "+ncdfe.getMessage(), ncdfe);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.error("AutoTypeDiscovery init failed", e);
        }
    }
    public String[] getInitAutoTypeList(){
        return initAutoTypeList;
    }
    public HashSet<String> getAutotypeClasses(){
        return autotypeClasses;
    }
    public void clearInstance(){
        this.allClasses = null;
        this.cantDeserialize.clear();
        this.cacheAllAssignMap.clear();
        this.cacheCreatorConstructor.clear();
        this.hasDiscovered.clear();
    }

    private void mergeToAutotypeClasses(HashSet<Class> shortTmp){
        for(Class clazz: shortTmp){
            mergeToAutotypeClasses(clazz);
        }
    }
    private boolean canBeDeserialized(Class clazz){
        // 此处做检查，如果不管怎么加载都不能使用 java bean deser 反序列化的，就会被丢弃
        // 其他地方的检查是不能使用父类反序列化的情况
        String classname = clazz.getName();
        /* TODO
            // 这里获取父类是否可以直接使用 isAssignableFrom
            // 保证两个 classloader 加载的类是可以通用的
            */
        List<Class> allAssign = getAllAssign(clazz);
        allAssign.add(clazz);
        for(Class pclazz:allAssign){
            if(jsonParserClasses.contains(pclazz.getName())){
                return false;
            }
        }
        if(alreadyLoadedClasses.contains(classname)){
            return false;
        }
        if(cantDeserialize.contains(classname)){
            return false;
        }
        Constructor[] constructors = clazz.getDeclaredConstructors();
        boolean isInterfaceOrAbstract = clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
        boolean hasDefaultConstructor = hasDefaultConstructor(clazz, constructors);
        if(!isInterfaceOrAbstract && !hasDefaultConstructor){
            Pair<Constructor<?>,String[]> pair = getCreatorConstructor(clazz);
            String[] paramNames = pair.getValue();
            Constructor<?> creatorConstructor = pair.getKey();
            if(paramNames == null || creatorConstructor.getParameterTypes().length != paramNames.length){
                cantDeserialize.add(classname);
                return false;
            }
        }
        return true;
    }

    private boolean mergeToAutotypeClasses(Class clazz){
        if(canBeDeserialized(clazz)){
            if(this.autotypeClasses.add(clazz.getName()))
                AutoTypeGraphUtils.addNode(clazz);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean checkReferenceHandle(String name){
        name = TypeUtils.getPureName(name);
        if(this.autotypeClasses.contains(name)){
            return true;
        }
        return false;
    }

    // 该方法只能用于搜索超类
    private boolean findInAutotypeClasses(List<Class> shortTmp){
        for(Class clazz: shortTmp){
            if(banParentClasses.contains(clazz.getName())){
                return false;
            }
        }
        for(Class clazz: shortTmp){
            if(autotypeClasses.contains(clazz.getName())){
                return true;
            }
        }
        return false;
    }

    private Pair<Constructor<?>,String[]> getCreatorConstructor(Class clazz){
        String className = clazz.getName();
        if(cacheCreatorConstructor.containsKey(className)){
            return cacheCreatorConstructor.get(className);
        }
        if(cantDeserialize.contains(className)){
            return new Pair<>(null,null);
        }
        Constructor[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> creatorConstructor = null;

        String[] paramNames = null;

        for (Constructor constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();

            if (className.equals("org.springframework.security.web.authentication.WebAuthenticationDetails")) {
                if (parameterTypes.length == 2 && parameterTypes[0] == String.class && parameterTypes[1] == String.class) {
                    creatorConstructor = constructor;
                    creatorConstructor.setAccessible(true);
                    paramNames = ASMUtils.lookupParameterNames(constructor);
                    break;
                }
            }

            if (className.equals("org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken")) {
                if (parameterTypes.length == 3
                        && parameterTypes[0] == Object.class
                        && parameterTypes[1] == Object.class
                        && parameterTypes[2] == Collection.class) {
                    creatorConstructor = constructor;
                    creatorConstructor.setAccessible(true);
                    paramNames = new String[] {"principal", "credentials", "authorities"};
                    break;
                }
            }

            if (className.equals("org.springframework.security.core.authority.SimpleGrantedAuthority")) {
                if (parameterTypes.length == 1
                        && parameterTypes[0] == String.class) {
                    creatorConstructor = constructor;
                    paramNames = new String[] {"authority"};
                    break;
                }
            }

            boolean is_public = (constructor.getModifiers() & Modifier.PUBLIC) != 0;
            if (!is_public) {
                continue;
            }
            String[] lookupParameterNames = ASMUtils.lookupParameterNames(constructor);
            if (lookupParameterNames == null || lookupParameterNames.length == 0) {
                continue;
            }

            if (creatorConstructor != null
                    && paramNames != null && lookupParameterNames.length <= paramNames.length) {
                continue;
            }

            paramNames = lookupParameterNames;
            creatorConstructor = constructor;
        }
        return new Pair<>(creatorConstructor, paramNames);
    }

    public HashSet<Class> discoverFromClass(Class clazz) throws IOException {
        hasDiscovered.add(clazz.getName());
        HashSet<Class> shortTmp = new HashSet<>();
        Constructor[] constructors = clazz.getDeclaredConstructors();
        boolean isInterfaceOrAbstract = clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
        boolean hasDefaultConstructor = hasDefaultConstructor(clazz, constructors);
        Field[] declaredFields = clazz.getDeclaredFields();
        List<Field> fieldList = new ArrayList<>();
        if(!hasDefaultConstructor && !isInterfaceOrAbstract){
            Class<?>[] types = null;
            Pair<Constructor<?>,String[]> pair = getCreatorConstructor(clazz);
            String[] paramNames = pair.getValue();
            Constructor<?> creatorConstructor = pair.getKey();
            if (paramNames != null) {
                types = creatorConstructor.getParameterTypes();
            }

            Type[] genericParameterTypes = creatorConstructor.getGenericParameterTypes();
            if (paramNames != null
                    && types.length == paramNames.length) {
                for (int i = 0; i < types.length && i < genericParameterTypes.length; ++i) {
                    String paramName = paramNames[i];

                    Type fieldType = genericParameterTypes[i];
                    Field field = TypeUtils.getField(clazz, paramName, declaredFields);
                    fieldList.add(field);
                    HashSet<Class> interClasses = getInternalClass(fieldType);
                    shortTmp.addAll(interClasses);
                    for(Class interClass: interClasses){
                        AutoTypeGraphUtils.addEdge(clazz.getName(), interClass.getName(), "ConstructorArgs");
                    }
                }

                if (!clazz.getName().equals("javax.servlet.http.Cookie")) {
                    return shortTmp;
                }
            } else {
                cantDeserialize.add(clazz.getName());
                return shortTmp;
            }
        }

        // 处理 set 方法
        Method[] methods = clazz.getMethods();
        for (Method method : methods) { //
            String methodName = method.getName();

            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            // support builder set
            Class<?> returnType = method.getReturnType();
            if (!(returnType.equals(Void.TYPE) || returnType.equals(method.getDeclaringClass()))) {
                continue;
            }

            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            Class<?>[] types = method.getParameterTypes();

            if (types.length == 0 || types.length > 2) {
                continue;
            }

            if (methodName.length() < 4 || !methodName.startsWith("set")) {
                continue;
            }

            char c3 = methodName.charAt(3);

            String propertyName;
            Field field = null;
            if (Character.isUpperCase(c3) //
                    || c3 > 512 // for unicode method name
            ) {
                propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            } else if (c3 == '_') {
                propertyName = methodName.substring(4);
                field = TypeUtils.getField(clazz, propertyName, declaredFields);
                if (field == null) {
                    String temp = propertyName;
                    propertyName = methodName.substring(3);
                    field = TypeUtils.getField(clazz, propertyName, declaredFields);
                    if (field == null) {
                        propertyName = temp; //减少修改代码带来的影响
                    }
                }
            } else if (c3 == 'f') {
                propertyName = methodName.substring(3);
            } else if (methodName.length() >= 5 && Character.isUpperCase(methodName.charAt(4))) {
                propertyName = TypeUtils.decapitalize(methodName.substring(3));
            } else {
                propertyName = methodName.substring(3);
                field = TypeUtils.getField(clazz, propertyName, declaredFields);
                if (field == null) {
                    continue;
                }
            }

            if (field == null) {
                field = TypeUtils.getField(clazz, propertyName, declaredFields);
            }

            if (field == null && types[0] == boolean.class) {
                String isFieldName = "is" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
                field = TypeUtils.getField(clazz, isFieldName, declaredFields);
            }
            if(field!=null){
                fieldList.add(field);
            }
            HashSet<Class> interClasses = getInternalClass(TypeUtils.getFieldType(method, field));
            shortTmp.addAll(interClasses);
            for(Class interClass: interClasses){
                AutoTypeGraphUtils.addEdge(clazz.getName(), interClass.getName(), "SetterArgs");
            }
        }

        Field[] fields = clazz.getFields();
        List<Class> interFields = computeFields(fieldList, fields);
        shortTmp.addAll(interFields);
        for(Class interClass: interFields){
            AutoTypeGraphUtils.addEdge(clazz.getName(), interClass.getName(), "PublicField");
        }

        for (Method method :methods) { // getter methods
            String methodName = method.getName();
            if (methodName.length() < 4) {
                continue;
            }

            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (methodName.startsWith("get") && Character.isUpperCase(methodName.charAt(3))) {
                if (method.getParameterTypes().length != 0) {
                    continue;
                }

                if (Collection.class.isAssignableFrom(method.getReturnType()) //
                        || Map.class.isAssignableFrom(method.getReturnType()) //
                        || AtomicBoolean.class == method.getReturnType() //
                        || AtomicInteger.class == method.getReturnType() //
                        || AtomicLong.class == method.getReturnType() //
                ) {
                    String propertyName;
                    propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                    boolean contains = false;
                    for (Field item : fieldList) {
                        if (item.getName().equals(propertyName)) {
                            contains = true;
                            break;
                        }
                    }
                    if(!contains){
                        HashSet<Class> interClasses = getInternalClass(TypeUtils.getFieldType(method,null));
                        shortTmp.addAll(interClasses);
                        for(Class interClass: interClasses){
                            AutoTypeGraphUtils.addEdge(clazz.getName(), interClass.getName(), "GetterArgs");
                        }
                    }
                }
            }
        }
        return shortTmp;
    }

    private List<Class> computeFields(List<Field> fieldList, Field[] fields) throws IOException {
        List<Class> shortTmp = new ArrayList<>();
        for (Field field : fields) { // public static fields
            int modifiers = field.getModifiers();
            if ((modifiers & Modifier.STATIC) != 0) {
                continue;
            }

            if ((modifiers & Modifier.FINAL) != 0) {
                Class<?> fieldType = field.getType();
                boolean supportReadOnly = Map.class.isAssignableFrom(fieldType)
                        || Collection.class.isAssignableFrom(fieldType)
                        || AtomicLong.class.equals(fieldType) //
                        || AtomicInteger.class.equals(fieldType) //
                        || AtomicBoolean.class.equals(fieldType);
                if (!supportReadOnly) {
                    continue;
                }
            }

            boolean contains = false;
            for (Field item : fieldList) {
                if (item.getName().equals(field.getName())) {
                    contains = true;
                    break;
                }
            }

            if (contains) {
                continue;
            }

            shortTmp.addAll(getInternalClass(field.getGenericType()));
            fieldList.add(field);
        }
        return shortTmp;
    }

    public HashSet<Class> getInternalClass(Type[] types) throws IOException {
        HashSet<Class> results = new HashSet<>();
        for(Type type: types){
            HashSet<Class> tmp = getInternalClass(type);
            results.addAll(tmp);
        }
        return results;
    }

    public HashSet<Class> getInternalClass(Type type) throws IOException {
        HashSet<Class> results = new HashSet<>();
        if(type instanceof ParameterizedType){
            ParameterizedType ptype = (ParameterizedType)type;
            // 特例
            // 为了适配 com.alibaba.fastjson.util.TypeUtils#createCollection 函数的 bug
            // 直接实例化了一些接口
            // isAssignableFrom 还写反了，我佛了
            if(ptype.getRawType() instanceof Class && java.util.concurrent.BlockingQueue.class.isAssignableFrom((Class<?>) ptype.getRawType())){
                return results;
            }
            HashSet<Class> rawClasses = getInternalClass(ptype.getRawType());
            results.addAll(rawClasses);
            Type[] ataTypes = ptype.getActualTypeArguments();
            for(Type ataType : ataTypes){
                HashSet<Class> internalType = getInternalClass(ataType);
                results.addAll(internalType);
            }
        }
        else if(type instanceof GenericArrayType){
            GenericArrayType gaType = (GenericArrayType)type;
            results.addAll(getInternalClass(gaType.getGenericComponentType()));
        }
        else if(type instanceof TypeVariable){
            TypeVariable tvType = (TypeVariable) type;
            /* TODO
            // 暂时不保存范型的代指类型
            // 要注意的是，如果一个范型明确了为某个class，只有这个范型在他所被声明的类中发生set的时候，class才允许被反序列化
            // 此处为了方便处理，做了很大的简化
             */
            for(Type tvBound: tvType.getBounds()){
                if(tvBound instanceof Class && !tvBound.equals(Object.class)){
                    results.addAll(getInternalClass(tvBound));
                }
            }
        }
        else if(type instanceof WildcardType){
            WildcardType wType = (WildcardType) type;
            Type[] ubType = wType.getUpperBounds();
            if(!ubType[0].equals(Object.class)){
                results.addAll(getInternalClass(ubType));
            }
        }
        else if(type instanceof Class){
            Class cType = (Class)type;
            if(cType.isPrimitive()){
                return results;
            }
            if(cType.isArray()){
                results.addAll(getInternalClass(cType.getComponentType()));
                return results;
            }
            results.add(cType);
        }
        else{
            throw new IOException("type get failed");
        }
        return results;
    }
    public static boolean hasDefaultConstructor(Class clazz, Constructor[] constructors){
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }

//        Constructor<?> defaultConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
//                defaultConstructor = constructor;
                return true;
            }
        }

        if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            Class<?>[] types;
            for (Constructor<?> constructor : constructors) {
                if ((types = constructor.getParameterTypes()).length == 1
                        && types[0].equals(clazz.getDeclaringClass())) {
//                    defaultConstructor = constructor;
                    return true;
                }
            }
        }
        return false;
    }

    public void save() throws IOException {
        AutoTypeGraphUtils.save();
    }

    public List<Class> getAllAssign(Class clazz){
        String classname = clazz.getName();
        if(cacheAllAssignMap.containsKey(classname)){
            return cacheAllAssignMap.get(classname);
        }
        List<Class> assignableClasses = new ArrayList<>();
        if(clazz.isPrimitive()){
            return assignableClasses;
        }
        List<Class> listSuperClass = new ArrayList<>();
        if(!clazz.isInterface()){
            Class superclass = clazz.getSuperclass();
            while (superclass != null && !superclass.getName().equals("java.lang.Object")) {
                listSuperClass.add(superclass);
                superclass = superclass.getSuperclass();
            }
            assignableClasses.addAll(listSuperClass);
        }
        Stack<Class> stack = new Stack<>();
        stack.addAll(listSuperClass);
        stack.push(clazz);
        while(!stack.isEmpty()){
            Class interfaces[] = stack.pop().getInterfaces();
            for(Class inter: interfaces){
                if(!assignableClasses.contains(inter)){
                    assignableClasses.add(inter);
                    stack.push(inter);
                }
            }
        }
        // 这里加一个派生的黑名单
        assignableClasses.removeAll(cantExpect);
        cacheAllAssignMap.put(classname, assignableClasses);
        return new ArrayList<>(assignableClasses);
    }

    public void discover() throws Exception {
        if(this.cacheLoaded){
            return;
        }
        int autotypeNum = autotypeClasses.size();
        while(true){
            for(Class clazz: this.allClasses){
                if(hasDiscovered.contains(clazz.getName())){
                    continue;
                }
                if(!BlackList.check(clazz.getName())){
                    continue;
                }
                try {
                    if(!canBeDeserialized(clazz)){
                        continue;
                    }
                    if(!Modifier.isPublic(clazz.getModifiers()) && !hasDefaultConstructor(clazz, clazz.getDeclaredConstructors())) {
                        continue;
                    }
                    if (clazz.isMemberClass()) {
                        if(!Modifier.isStatic(clazz.getModifiers()) && !autotypeClasses.contains(clazz.getDeclaringClass().getName()))
                            continue;
                    }
                    if(clazz.isAnonymousClass()){
                        continue;
                    }
                    List<Class> assignableClasses = getAllAssign(clazz);
                    if(findInAutotypeClasses(assignableClasses)){
                        for(Class assignableClass: assignableClasses)
                            AutoTypeGraphUtils.addEdge(assignableClass.getName(), clazz.getName(), "Inherit");
                        mergeToAutotypeClasses(clazz);
                        mergeToAutotypeClasses(discoverFromClass(clazz));
                    }
                }catch (NoClassDefFoundError | TypeNotPresentException | IncompatibleClassChangeError |
                        java.lang.VerifyError | MalformedParameterizedTypeException | SecurityException ncdfe){
                    //
                }
            }
            if(autotypeNum == autotypeClasses.size()){
                break;
            }
            else {
                autotypeNum = autotypeClasses.size();
            }
        }
    }
    public static void main(String[] args) throws Exception {
        URL[] u = new URL[]{Paths.get("FullNode.jar").toUri().toURL()};
        System.out.println("[+] Load jdk classes and External specified classes");
        final ClassLoader classLoader = new URLClassLoader(u);
        final ReflectClassEnumerator rce = new ReflectClassEnumerator(classLoader);
        AutoTypeDiscovery atd = new AutoTypeDiscovery(rce);
        System.out.println("[+] start discover ...");
        atd.discover();
        System.out.println("[+] save data to cytoscape for visualization");
        atd.save();
        System.out.println("[+] complete! clear memory and exit.");
        atd.clearInstance();
    }
}

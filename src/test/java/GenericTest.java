import org.junit.Test;
import sun.tools.tree.SuperExpression;

import javax.swing.text.StyledEditorKit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ewan on 28/08/2017.
 */
public class GenericTest {
    //java泛型只在编译阶段有效，不会进入运行阶段。编译时正确检验泛型结果后，会将泛型擦除，从而如下例变成相同的基本类型。
    //因此java泛型只有伪泛型，并不能对性能有提升（因为没有进入运行阶段）
    //泛型作用之一：提供编译检查，在编译时检查对List<String>加进去的元素都是String类型。
    //泛型作用之二：代码复用（通配符）
    //把List<Integer>改为List<int>会编译错误，这也说明了java泛型会在编译时把泛型类型擦除为Object
    //-128到127的int装箱结果会被缓存，以提升性能
    //装箱缓存：–128到127的int或short，'\u0000'到'\u007f'间的char，byte，boolean.
    //为什么缓存这个范围，因为很多算法都会使用小数值计算。这个范围可以通过-XX:AutoBoxCacheMax改变。

//    error compile：
//    public static String a(List<Integer> dd) {
//        return dd.toString();
//    }
//    public static String a(List<String> dd) {
//        return dd.toString();
//    }

//    public static String a(List<int> dd) {
//
//    }

    @Test
    public void sameClass() {
        List<String> stringArrayList = new ArrayList<>();
        List<Integer> integerArrayList = new ArrayList<>();
        integerArrayList.add(1);
        integerArrayList.get(0);

        assertEquals(stringArrayList.getClass(), integerArrayList.getClass());
    }

    private int sum(List<Integer> ints) {
        int s = 0;
        for (int n : ints) {
            s += n;
        }
        return s;
    }

    private Integer sumInteger(List<Integer> ints) {
        Integer s = 0;
        for (Integer n : ints) {
            s += n;
        }
        return s;
    }

    @Test
    public void Perf() {
        List<Integer> integerList = new ArrayList<>();
        for (Integer i = 0; i < 10000000; i++) {
            integerList.add(i);
        }

        long startTime = System.currentTimeMillis();
        sum(integerList);
        System.out.println("sum:" + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        sumInteger(integerList);
        System.out.println("sum:" + (System.currentTimeMillis() - startTime));
        //sum:unboxes each ele; sumInteger: unboxes each ele to perform the addition,then boxes each ele.
    }

    @Test
    public void boxingCache1() {
        List<Integer> bigs = Arrays.asList(100, 200, 300);
        assertTrue(sumInteger(bigs) != sumInteger(bigs));
    }

    @Test
    public void boxingCache2() {
        List<Integer> bigs = Arrays.asList(2, 3, 4);
        assertTrue(sumInteger(bigs) == sum(bigs));
        assertTrue(sumInteger(bigs) != sumInteger(bigs));
    }

    @Test
    public void ListObject() {
        List arrayList = new ArrayList();
        arrayList.add("aaaa");
        arrayList.add(100);

        //会输出为class java.lang.String
        System.out.println(arrayList.get(0).getClass());
        //会输出为class java.lang.Integer
        System.out.println(arrayList.get(1).getClass());
    }

    @Test
    public void wildCardAndGenericType() {
        List<Integer> integerList = new ArrayList<>();
        //compile Error
        //System.out.println(count(integerList));

        //一个限定
        integerList.add(1);
        System.out.println(countNumberList(integerList));
        List<Long> longList = new ArrayList<>();
        longList.add(new Long(0));
        System.out.println(countList(longList));

        //?-接口也是extends
        //多个约束的话需要用到泛型方法，前面写上<T>才会被识别为泛型方法
        integerList.add(2);
        System.out.println(compare(integerList));
        System.out.println(compare1(integerList));
        System.out.println(compare2(integerList));
    }

    private int count(List<Number> numberList) {
        return numberList.size();
    }

    //两种写法
    private int countNumberList(List<? extends Number> list) {
        return list.get(0).intValue() + 1;
    }

    private <T extends Number> int countList(List<T> list) {
        return list.size();
    }

    //三种写法
    private <T extends Number & Comparable<T>> int
    compare(List<T> list) {
        return list.get(0).compareTo(list.get(1));
    }

    private <T extends Number & Comparable<T>> int
    compare1(List<? extends T> list) {
        return list.get(0).compareTo(list.get(1));
    }

    private <T extends Number & Comparable<? super T>> int
    compare2(List<T> list) {
        return list.get(0).compareTo(list.get(1));
    }


    @Test
    public void compileError() {
//        List<String>[] a = new ArrayList<String>[10];
    }

    //为什么new ArrayList<String>[10]不允许？是因为ArrayList<String>和ArrayList<Integer>等其实是同一种类型
    //ArrayList<String> stringLists = new ArrayList<String>[10];  // compiler error, but pretend it's allowed
    //stringLists[0] = new ArrayList<String>();   // OK
    //stringLists[1] = new ArrayList<Integer>();  // ArrayList<String>应该要有String的类型检查
    // 但是因为泛型擦出，在运行时却检查不出来
    @Test
    public void implicitConversion() {
        //ArrayList没限定类型，因此是允许的。但存在潜在的类型转换错误的风险。
        List<String>[] a = (List<String>[]) new ArrayList[10];

        List<? extends String>[] b = new ArrayList[10];
    }

    //use T other than ?
    public void copyUsingWildCard(List<? extends Number> dest, List<? extends Number> src) {
        for (Number number: src) {
            //compile error，因为dest和src潜在的类型不匹配
            //dest.add(number);
        }
    }

    public <T extends Number> void copyUsingGenericType(List<T> dest, List<T> src) {
        for (T number: src) {
            dest.add(number);
        }
    }
    //above，继承多个时也用T

    // use ? other than T
    public void print(List<? super Integer> list){

    }
    //compile error
//    public <T super Integer> print(List<? super Integer> list){
//
//    }

    /**
     * 泛型方法的基本介绍
     *
     * @param tClass 传入的泛型实参
     * @return T 返回值为T类型
     * 说明：
     * 1）public 与 返回值中间<T>非常重要，可以理解为声明此方法为泛型方法。
     * 2）只有声明了<T>的方法才是泛型方法，泛型类中的使用了泛型的成员方法并不是泛型方法。
     * 3）<T>表明该方法将使用泛型类型T，此时才可以在方法中使用泛型类型T。
     * 4）与泛型类的定义一样，此处T可以随便写为任意标识，常见的如T、E、K、V等形式的参数常用于表示泛型。
     */
    private <T> T genericMethod(Class<T> tClass) throws InstantiationException,
            IllegalAccessException {
        T instance = tClass.newInstance();
        return instance;
    }

    @Test
    public void extensibleParamsUsingGeneric() {
        printMsg(1, "dfsd", true, 'C');
    }

    private <T> void printMsg(T... args) {
        for (T t :
                args) {
            System.out.println(t + ",");
        }
    }
}

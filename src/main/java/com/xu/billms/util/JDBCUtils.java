package com.xu.billms.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.xu.billms.exception.RecordNotUniqueException;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 数据连接工具
 *
 * @author xu
 */
public class JDBCUtils {

    private static DataSource ds;

    static {
        try {
            // 初始化数据连接池
            InputStream in = JDBCUtils.class.getClassLoader().getResourceAsStream("jdbc.properties");
            // 加载资源文件
            Properties props = new Properties();
            props.load(in);
            // druid使用资源文件创建数据连接池
            ds = DruidDataSourceFactory.createDataSource(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构造方法私有化,不能直接创建对象
     */
    private JDBCUtils() {
    }

    /**
     * 获取数据连接对象
     */
    public static Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭并释放资源
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement stmt, Connection conn) {
        close(null, stmt, conn);
    }

    /**
     * 对表中数据更新
     * @param sql 需要执行跟新sql语句
     * @param params 修改对应的参数
     * @return 修改成功返回1,失败-1
     */
    public static int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            // 获取连接
            conn = getConnection();
            // 获取执行sql语句的对象
            ps = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    // 设置参数
                    ps.setObject(i + 1, params[i]);
                }
            }
            // 执行sql语句
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放
            close(ps, conn);
        }
        return -1;
    }
    // ResultSet rs = executeQuery(sql);

    /**
     * 执行查询方法
     *
     * @param aClass 指定需要返回数据的具体的类型
     * @param sql    执行查询的SQL语句
     * @param params 查询的条件
     */
    public static <T> List<T> executeQuery(Class<T> aClass, String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> list = null;
        try {
            // 获取连接
            conn = getConnection();
            // 获取执行sql语句的预编译对象
            ps = conn.prepareStatement(sql);
            // 设置参数
            // 判断参数是否为空,
            // 如果为空,则表明当前sql语句中没有参数需要设置
            if (params != null) {

                // 遍历参数
                for (int i = 0; i < params.length; i++) {
                    if (params[i] instanceof Collection) {
                        List param = (List) params[i];
                        for (int j = 0; j < param.size(); j++) {
                            ps.setObject(j + 1, param.get(j));
                        }

                    } else {
                        ps.setObject(i + 1, params[i]);
                    }
                }
            }
            // 执行sql
            rs = ps.executeQuery();
            // 对集合进行初始化
            list = new ArrayList<>();
            while (rs.next()) {
                // 获取指定的类型的实例对象,相当于:User user = new User();
                T obj = aClass.newInstance();
                // 获取返回结果集的元数据集合,在这个集合中,可以获取到结果集有多少列
                // 元数据集合
                ResultSetMetaData metaData = rs.getMetaData();
                // 获取集合中的列
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    // 获取列的名称
                    String columnName = metaData.getColumnName(i);
                    // 把结果集中列和实例对象中的属性做对应
                    // 属性描述器,描述一个属性,
                    // 方法有:
                    // getPropertyType():获取属性的类型
                    // getReadMethod():  获取读取属性方法
                    // getWriteMethod():  获取写入属性方法

                    // TODO 把列名转换为驼峰命名的字段名
                    String fieldName = StringUtils.underLineToHump(columnName);
                    try {
                        PropertyDescriptor pd = new PropertyDescriptor(fieldName, aClass);
                        // 获取写入属性的方法
                        Method writeMethod = pd.getWriteMethod();
                        // 获取该列多对应的值
                        Object value = rs.getObject(columnName);
                        // 调用写入属性的方法,把属性设置到该对象中
                        writeMethod.invoke(obj, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                list.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放
            close(rs, ps, conn);
        }
        return list;
    }

    /**
     *
     * @param aClass 实体字节码对象
     * @param sql 需要执行的sql语句
     * @param params 对应的参数
     * @param <T> 泛型
     * @return 返回List第一个对象
     */
    public static <T> T executeQueryForOne(Class<T> aClass, String sql, Object... params) {
        List<T> users = executeQuery(aClass, sql, params);
        System.out.println("params" + params);
        if (users != null && users.size() > 0) {
            if (users.size() > 1) {
                // 抛出异常
                System.out.println("记录数大于1");
                throw new RecordNotUniqueException("记录数大于1");
            }
            return users.get(0);
        }
        return null;
    }

    public static Integer executeQueryForCount(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 获取连接
            conn = getConnection();
            // 获取执行sql语句的预编译对象
            ps = conn.prepareStatement(sql);
            // 设置参数
            // 判断参数是否为空,
            // 如果为空,则表明当前sql语句中没有参数需要设置
            if (params != null) {
                // 遍历参数
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            // 执行sql
            rs = ps.executeQuery();
            // 判断是否有结果
            if (rs.next()) {
                // 获取数据
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放
            close(rs, ps, conn);
        }
        return null;
    }


}

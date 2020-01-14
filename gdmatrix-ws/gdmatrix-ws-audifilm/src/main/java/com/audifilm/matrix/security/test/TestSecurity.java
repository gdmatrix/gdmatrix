package com.audifilm.matrix.security.test;

import com.audifilm.matrix.security.service.SecurityManager;
import java.util.List;
import org.matrix.security.AccessControl;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author comasfc
 */
public class TestSecurity {


  static public void main(String [] args) throws Exception {

//    configureDataSource();
    MatrixConfig.setProperty(MatrixConfig.MATRIX_CONFIG_DIR_PROPERTY, "./conf");

    SecurityManager manager = new SecurityManager();
    List<AccessControl> llista = manager.findAccessControlList(org.matrix.cases.Case.class.getPackage().getName(), "comasfc");

    for(AccessControl ac : llista) {
      System.out.println(ac.getRoleId() + " - " + ac.getAction());
    }

  }

  /*
  static  public void configureDataSource() {
    try {

      
      System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
      System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
      InitialContext ic = new InitialContext();
      ic.createSubcontext("java:");
      ic.createSubcontext("java:/comp");
      ic.createSubcontext("java:/comp/env");
      ic.createSubcontext("java:/comp/env/jdbc");


      // Construct DataSource
      OracleConnectionPoolDataSource ds = new OracleConnectionPoolDataSource();
      ds.setURL("jdbc:oracle:thin:@xxxxx:yyyyy:zzzzz");
      ds.setUser("******");
      ds.setPassword("******");


      ic.bind("java:comp/env/jdbc/xxxxxx", ds);
      } catch (Exception e) {
        // refactor
        e.printStackTrace();
      }
    }
 */
/*
  static public void configureDataSource() throws SQLException, NamingException {
		OracleDataSource ds = new OracleDataSource();

		ds.setDescription(
		"xxxxx");
		ds.setServerName("xxxxx");
		ds.setPortNumber(0);
		ds.setUser("******");
		ds.setPassword("******");

 		// Set up environment for creating initial context
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
		"com.sun.jndi.fscontext.RefFSContextFactory");
		env.put(Context.PROVIDER_URL, "file:c:\\JDBCDataSource");
		Context ctx = new InitialContext(env);

		// Register the data source to JNDI naming service
		ctx.bind("jdbc/xxxxxx", ds);
  }

    static public void configureDataSource() {
      try {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        InitialContext ic = new InitialContext();
        ic.createSubcontext("java:");
        ic.createSubcontext("java:/comp");
        ic.createSubcontext("java:/comp/env");
        ic.createSubcontext("java:/comp/env/jdbc");

        // Construct DataSource
        OracleConnectionPoolDataSource ds = new OracleConnectionPoolDataSource();
        ds.setURL("jdbc:oracle:thin:@xxxxx:yyyyy:zzzzz");
        ds.setUser("******");
        ds.setPassword("******");

        ic.bind("java:/comp/env/jdbc/xxxxxx", ds);
    } catch (Exception e) { // refactor
      e.printStackTrace();
    }
  }
*/
}

package info.syachiku.sample;
import java.util.*;
import java.util.function.Function;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import info.syachiku.sample.OrderData.*;
import static org.junit.jupiter.api.Assertions.*;
class ServiceSplittingTest {
    @Configuration @EnableTransactionManagement
    static class Config {
        @Bean DataSource dataSource() {
            var ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
            return ds;
        }
        @Bean JdbcTemplate jdbc(DataSource ds) { return new JdbcTemplate(ds); }
        @Bean PlatformTransactionManager transactionManager(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean LabPorts ports(JdbcTemplate jdbc) { return new LabPorts(jdbc); }
        @Bean BeforeOrderService before(LabPorts p) { return new BeforeOrderService(p); }
        @Bean OrderPriceCalculator calculator() { return new OrderPriceCalculator(); }
        @Bean OrderNotifier notifier(LabPorts p) { return new OrderNotifier(p); }
        @Bean AfterOrderService after(LabPorts p, OrderPriceCalculator c, OrderNotifier n) { return new AfterOrderService(p,c,n); }
    }
    static class LabPorts implements OrderPorts {
        final JdbcTemplate db;
        final List<String> calls = new ArrayList<>();
        final List<Long> sent = new ArrayList<>();
        String failure = "none";
        LabPorts(JdbcTemplate db) { this.db=db; }
        public Customer findCustomer(long id) {
            calls.add("customer");
            return db.queryForObject("select id,premium,email from customers where id=?", (r,n)->new Customer(r.getLong(1),r.getBoolean(2),r.getString(3)),id);
        }
        public List<Product> findProducts(List<Long> ids) {
            calls.add("products");
            return ids.stream().map(id->db.queryForObject("select id,price,stock from products where id=?",(r,n)->new Product(r.getLong(1),r.getInt(2),r.getInt(3)),id)).toList();
        }
        public void decreaseStock(Product p) { calls.add("stock:"+p.id()); db.update("update products set stock=stock-1 where id=?",p.id()); }
        public long save(long id,int total) {
            calls.add("save:"+total);
            if (failure.equals("save")) throw new IllegalStateException("save failed");
            db.update("insert into orders values(100,?,?)",id,total); return 100;
        }
        public void sendOrderAccepted(String email,long id) {
            calls.add("mail:"+id);
            if (failure.equals("mail")) throw new IllegalStateException("mail failed");
            sent.add(id);
        }
        public void writeAudit(String event,long id) {
            calls.add("audit:"+id);
            if (failure.equals("audit")) throw new IllegalStateException("audit failed");
            db.update("insert into audit_log values(?,?)",event,id);
        }
    }
    @ParameterizedTest
    @CsvSource({"before,none,true,2,2700", "after,none,true,2,2700", "before,none,false,2,3000", "after,none,false,2,3000", "before,stock,true,0,2700", "after,stock,true,0,2700", "before,save,true,2,2700", "after,save,true,2,2700", "before,mail,true,2,2700", "after,mail,true,2,2700", "before,audit,true,2,2700", "after,audit,true,2,2700"})
    void preservesContract(String version,String failure,boolean premium,int secondStock,int total) {
        try (var ctx=new AnnotationConfigApplicationContext(Config.class)) {
            var db=ctx.getBean(JdbcTemplate.class); var p=ctx.getBean(LabPorts.class);
            db.execute("create table customers(id bigint primary key,premium boolean,email varchar(100))");
            db.execute("create table products(id bigint primary key,price int,stock int)");
            db.execute("create table orders(id bigint primary key,customer_id bigint,total int)");
            db.execute("create table audit_log(event varchar(40),order_id bigint)");
            db.update("insert into customers values(1,?,'sample@example.test')",premium);
            db.update("insert into products values(10,1000,2),(20,2000,?)",secondStock);
            p.failure=failure;
            Function<Request,Long> operation=version.equals("before")?ctx.getBean(BeforeOrderService.class)::placeOrder:ctx.getBean(AfterOrderService.class)::placeOrder;
            var request=new Request(1,List.of(10L,20L));
            if (failure.equals("none")) assertEquals(100L,operation.apply(request));
            else assertEquals(failure.equals("stock")?"out-of-stock:20":failure+" failed",assertThrows(IllegalStateException.class,()->operation.apply(request)).getMessage());
            var expected=new ArrayList<>(List.of("customer","products","stock:10"));
            if (!failure.equals("stock")) {
                expected.add("stock:20"); expected.add("save:"+total);
                if (!failure.equals("save")) {
                    expected.add("mail:100");
                    if (!failure.equals("mail")) expected.add("audit:100");
                }
            }
            assertEquals(expected,p.calls,"fixed call order and count");
            boolean ok=failure.equals("none");
            assertEquals(ok?1:2,db.queryForObject("select stock from products where id=10",Integer.class));
            assertEquals(ok?secondStock-1:secondStock,db.queryForObject("select stock from products where id=20",Integer.class));
            assertEquals(ok?1:0,db.queryForObject("select count(*) from orders",Integer.class));
            assertEquals(ok?1:0,db.queryForObject("select count(*) from audit_log",Integer.class));
            assertEquals(ok||failure.equals("audit")?List.of(100L):List.of(),p.sent,"external mail is not rolled back");
            if (ok) assertEquals(total,db.queryForObject("select total from orders where id=100",Integer.class));
        }
    }
    @Test void calculatorReturnsBreakdown() {
        assertEquals(new Price(3000,300,2700),new OrderPriceCalculator().calculate(new Customer(1,true,"x"),List.of(new Product(10,1000,2),new Product(20,2000,2))));
    }
    @Test void discountIsTruncatedBeforeSubtracting() {
        assertEquals(new Price(1001,100,901),new OrderPriceCalculator().calculate(new Customer(1,true,"x"),List.of(new Product(10,1001,2))));
    }
}

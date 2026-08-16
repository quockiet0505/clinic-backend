import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CleanDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/clinic_system?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull";
        String user = "root";
        String password = "12345678";
        String email = "quockietdev@gmail.com";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to the database!");
            
            // Delete patient vital profile
            String sql1 = "DELETE FROM patient_vital_profile WHERE patient_id IN (SELECT patient_id FROM patient WHERE account_id IN (SELECT account_id FROM account WHERE email=?))";
            try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                pstmt.setString(1, email);
                pstmt.executeUpdate();
            } catch(Exception e) { System.out.println(e.getMessage()); }
            
            // Delete patient
            String sql2 = "DELETE FROM patient WHERE account_id IN (SELECT account_id FROM account WHERE email=?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                pstmt.setString(1, email);
                pstmt.executeUpdate();
            } catch(Exception e) { System.out.println(e.getMessage()); }
            
            // Delete account_role
            String sql3 = "DELETE FROM account_roles WHERE account_id IN (SELECT account_id FROM account WHERE email=?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql3)) {
                pstmt.setString(1, email);
                pstmt.executeUpdate();
            } catch(Exception e) { System.out.println(e.getMessage()); }
            
            // Delete account
            String sql4 = "DELETE FROM account WHERE email=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql4)) {
                pstmt.setString(1, email);
                int rows = pstmt.executeUpdate();
                System.out.println("Deleted account rows: " + rows);
            } catch(Exception e) { System.out.println(e.getMessage()); }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

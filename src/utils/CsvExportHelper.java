package utils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvExportHelper
{
    private static final String CSV_FILE_PATH = "submissions_data.csv";
    private static boolean firstWrite = true;

    public static void saveToCsv(String submitId, String userName, String sourceCode, String language)
    {
        try (FileWriter fw = new FileWriter(CSV_FILE_PATH, !firstWrite); PrintWriter pw = new PrintWriter(fw))
        {
            if (firstWrite)
            {
                pw.println("submit_id,user_name,language,code");
                firstWrite = false;
            }

            String safeCode = sourceCode.replace("\r", "\\r")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                    .replace("\"", "\"\"");

            String csvLine = String.format("\"%s\",%s,\"%s\",\"%s\"", submitId, userName, language, safeCode);
            pw.println(csvLine);
        }
        catch (IOException e)
        {
            System.out.println("Lỗi khi ghi file CSV: " + e.getMessage());
        }
    }
}
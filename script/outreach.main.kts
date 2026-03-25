@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("org.simplejavamail:simple-java-mail:8.11.1")
@file:DependsOn("org.slf4j:slf4j-simple:2.0.7")

import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import java.io.File
import java.time.LocalDateTime

/**
 * AI MOBILE HACKATHON 2026 - OUTREACH ENGINE
 * Coordinator: Kaustubh Deshpande
 */

val gmailUser = System.getenv("GMAIL_USER")
val gmailPass = System.getenv("GMAIL_PASS")
val subjectTemplate = System.getenv("EMAIL_SUBJECT") ?: "Join the AI Mobile Hackathon 2026, {{Name}}!"
val bodyTemplate = System.getenv("EMAIL_BODY") ?: "Hi {{Name}}, let's build AI apps!"

val csvFile = File("data/recipients.csv")
val sampleFile = File("data/recipients.sample.csv")

fun main() {
    println("--- Batch Started: ${LocalDateTime.now()} ---")

    if (gmailUser.isNullOrBlank() || gmailPass.isNullOrBlank()) {
        println("ERROR: Credentials missing in Environment Variables.")
        return
    }

    // Determine which file to use (Private CSV takes priority over Sample)
    val activeFile = if (csvFile.exists()) csvFile else sampleFile
    if (!activeFile.exists()) {
        println("ERROR: No data found in data/ folder.")
        return
    }

    val lines = activeFile.readLines().filter { it.isNotBlank() }
    if (lines.isEmpty()) return

    val header = lines[0]
    val records = lines.drop(1)
    val updatedLines = mutableListOf<String>()
    updatedLines.add(header) // Preserve the header at the top

    val mailer = MailerBuilder
        .withSMTPServer("smtp.gmail.com", 465, gmailUser, gmailPass)
        .withTransportStrategy(TransportStrategy.SMTPS)
        .buildMailer()

    var sentCount = 0
    val BATCH_LIMIT = 10

    for (line in records) {
        val cols = line.split(",").map { it.trim() }
        val name = cols.getOrNull(0)?.ifBlank { "Developer" } ?: "Developer"
        val email = cols.getOrNull(1)
        val status = cols.getOrNull(2) ?: ""

        // Logic: Send if status is empty OR contains an Error (Retry)
        val isAlreadySent = status.contains("Sent at", ignoreCase = true)
        val isPending = status.isBlank() || status.contains("Error", ignoreCase = true)

        if (sentCount < BATCH_LIMIT && !email.isNullOrBlank() && isPending && !isAlreadySent) {
            try {
                val personalSubject = subjectTemplate.replace("{{Name}}", name)
                val personalBody = bodyTemplate.replace("{{Name}}", name)

                val emailObj = EmailBuilder.startingBlank()
                    .from("Kaustubh Deshpande", gmailUser)
                    .to(name, email)
                    .withSubject(personalSubject)
                    .withHTMLText(personalBody)
                    .buildEmail()

                mailer.sendMail(emailObj)

                updatedLines.add("$name,$email,Sent at ${LocalDateTime.now()}")
                sentCount++
                println("SUCCESS: $name ($email)")

                Thread.sleep(3000) // Anti-spam delay
            } catch (e: Exception) {
                println("FAILED: $email - ${e.message}")
                updatedLines.add("$name,$email,Error: ${e.message?.take(15)}")
            }
        } else {
            // Keep the line as is (already sent or batch limit reached)
            updatedLines.add(line)
        }
    }

    // Rewrite the CSV with updated statuses
    activeFile.writeText(updatedLines.joinToString("\n"))
    println("--- Batch Complete. Total Sent: $sentCount ---")
}

main()
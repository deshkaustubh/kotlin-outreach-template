@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("org.simplejavamail:simple-java-mail:8.11.1")
@file:DependsOn("org.slf4j:slf4j-simple:2.0.7")

import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import java.io.File
import java.time.LocalDateTime
import java.util.Random

/**
 * GENERIC OUTREACH ENGINE (Template Version)
 * Features: Randomized Jitter, Anti-Injection, and Multi-part MIME (Plain Text + HTML)
 */

val gmailUser = System.getenv("GMAIL_USER")
val gmailPass = System.getenv("GMAIL_PASS")
val subjectTemplate = System.getenv("EMAIL_SUBJECT") ?: "Hello {{Name}}, reaching out!"
val bodyTemplate = System.getenv("EMAIL_BODY") ?: "Hi {{Name}},\n\nThis is a template email. Please update your GitHub Secrets to change this message."

val csvFile = File("data/recipients.csv")
val sampleFile = File("data/recipients.sample.csv")

fun main() {
    println("--- Batch Started: ${LocalDateTime.now()} ---")
    val random = Random()

    if (gmailUser.isNullOrBlank() || gmailPass.isNullOrBlank()) {
        println("ERROR: SMTP Credentials missing in GitHub Secrets.")
        return
    }

    val activeFile = if (csvFile.exists()) csvFile else sampleFile
    if (!activeFile.exists()) {
        println("ERROR: No recipient file found at data/recipients.csv")
        return
    }

    val lines = activeFile.readLines().filter { it.isNotBlank() }
    if (lines.isEmpty()) return

    val header = lines[0]
    val records = lines.drop(1)
    val updatedLines = mutableListOf<String>()
    updatedLines.add(header)

    val mailer = MailerBuilder
        .withSMTPServer("smtp.gmail.com", 465, gmailUser, gmailPass)
        .withTransportStrategy(TransportStrategy.SMTPS)
        .buildMailer()

    var sentCount = 0
    // Default limit for template users to prevent accidental spamming
    val BATCH_LIMIT = 5

    for (line in records) {
        val cols = line.split(",").map { it.trim() }
        val name = cols.getOrNull(0)?.ifBlank { "User" } ?: "User"
        val email = cols.getOrNull(1)
        val status = cols.getOrNull(2) ?: ""

        val isAlreadySent = status.contains("Sent at", ignoreCase = true)
        val isPending = status.isBlank() || status.contains("Error", ignoreCase = true)

        if (sentCount < BATCH_LIMIT && !email.isNullOrBlank() && isPending && !isAlreadySent) {
            try {
                // Anti-Injection: Ensures subject is clean plain text
                val cleanSubject = subjectTemplate
                    .replace("{{Name}}", name)
                    .replace(Regex("<[^>]*>"), "")
                    .replace("\n", " ")
                    .trim()

                val personalBody = bodyTemplate.replace("{{Name}}", name)

                val emailObj = EmailBuilder.startingBlank()
                    .from("Outreach Coordinator", gmailUser)
                    .to(name, email)
                    .withReplyTo(gmailUser)
                    .withSubject(cleanSubject)
                    .withHTMLText(personalBody)
                    // Plain Text fallback for better deliverability
                    .withPlainText("Hi $name,\n\nI am reaching out regarding our upcoming project. Please check the HTML version of this email for full details.\n\nBest regards.")
                    .buildEmail()

                mailer.sendMail(emailObj)

                updatedLines.add("$name,$email,Sent at ${LocalDateTime.now()}")
                sentCount++
                println("SUCCESS: Sent to $email")

                // Human-mimicry delay: 8s to 20s
                val waitTime = random.nextInt(12000) + 8000
                println("Waiting ${waitTime/1000}s...")
                Thread.sleep(waitTime.toLong())

            } catch (e: Exception) {
                println("FAILED: $email - ${e.message}")
                updatedLines.add("$name,$email,Error: ${e.message?.take(15)}")
            }
        } else {
            updatedLines.add(line)
        }
    }

    activeFile.writeText(updatedLines.joinToString("\n"))
    println("--- Batch Complete. Total Sent: $sentCount ---")
}

main()
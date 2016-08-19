import de.johoop.findbugs4sbt._
import de.johoop.cpd4sbt.CopyPasteDetector._
import de.johoop.cpd4sbt.{ReportType => CPDReportType}

// Put these into your project/plugins.sbt
// addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")
// addSbtPlugin("de.johoop" % "findbugs4sbt" % "1.3.0")
// addSbtPlugin("de.johoop" % "cpd4sbt" % "1.1.4")
// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.2.0")
// addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "<version>")

scalacOptions ++= Seq(
  "-feature",
  //"-deprecation",
  "-unchecked",
  "-Xlint",
  //"-Xstrict-inference",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  //"-Ywarn-numeric-widen",
  "-Ywarn-value-discard")

// Linter
//addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1-SNAPSHOT")
//addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.14")
scalacOptions += "-Xplugin:/home/hairy/dev/linter/target/scala-2.11/linter_2.11-0.1-SNAPSHOT.jar"
scalacOptions += "-P:linter:disable:UseHypot+CloseSourceFile"
scalacOptions += "-P:linter:printWarningNames"

// Scalastyle
scalastyleConfig <<= baseDirectory { base => base / "sca" / "scalastyle-config.xml" }
watchSources += baseDirectory.value / "sca" / "scalastyle-config.xml"

// Scapegoat
//scapegoatDisabledInspections := Seq("VarUse", "NullParameter", "NullAssignment", "WildcardImport")

// Findbugs (optionally put findbugs plugins (such as fb-contrib and findsecbugs) jars into ~/.findbugs/plugin)
findbugsSettings
//findbugsEffort := Effort.Maximum
findbugsReportPath <<= baseDirectory { base => Some(base / "sca" / "findbugsoutput.xml") }

// CPD
cpdSettings
cpdTargetPath <<= baseDirectory { base => base / "sca" }
cpdReportName := "cpdoutput.txt"
cpdReportType := CPDReportType.Simple

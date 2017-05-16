import de.johoop.findbugs4sbt._
import de.johoop.cpd4sbt.CopyPasteDetector._
import de.johoop.cpd4sbt.{ReportType => CPDReportType}

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
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
//addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.15")
scalacOptions += "-Xplugin:/home/hairy/dev/linter/target/scala-2.12/linter_2.12-0.1-SNAPSHOT.jar"
scalacOptions += "-P:linter:disable:UseHypot+CloseSourceFile"
scalacOptions += "-P:linter:printWarningNames"

// Scalastyle
scalastyleConfig := baseDirectory.value / "sca" / "scalastyle-config.xml"
watchSources += baseDirectory.value / "sca" / "scalastyle-config.xml"

// Scapegoat
//scapegoatDisabledInspections := Seq("VarUse", "NullParameter", "NullAssignment", "WildcardImport")

// Findbugs (optionally put findbugs plugins (such as fb-contrib and findsecbugs) jars into ~/.findbugs/plugin)
findbugsSettings
//findbugsEffort := Effort.Maximum
findbugsReportPath := Some(baseDirectory.value / "sca" / "findbugsoutput.xml")

// CPD
cpdSettings
cpdTargetPath := baseDirectory.value / "sca"
cpdReportName := "cpdoutput.txt"
cpdReportType := CPDReportType.Simple

package org.jetbrains.plugins.template.listeners


import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.jetbrains.lang.dart.DartLanguage
import io.flutter.sdk.FlutterSdkUtil
import org.jetbrains.kotlin.idea.core.deleteSingle
import org.jetbrains.plugins.template.services.MyProjectService
import org.jetbrains.plugins.template.utils.Utils.FILE_FOLDER
import org.jetbrains.plugins.template.utils.Utils.FILE_NAME


internal class OpenStartUp : StartupActivity {

    override fun runActivity(project: Project) {
        val isFlutter = FlutterSdkUtil.hasFlutterModules()
        if (!isFlutter) return
        createPsiFile(project)
    }

    private fun createPsiFile(project: Project) {
        WriteCommandAction.runWriteCommandAction(project) {
            val path = project.basePath?.plus("/$FILE_FOLDER") ?: throw Exception("folder not found")
            val vfsFile = VfsUtil.createDirectories(path)
            val file = PsiFileFactory.getInstance(project).createFileFromText(FILE_NAME, DartLanguage.INSTANCE, AbcContentFile.content())
            val directory = PsiDirectoryFactory.getInstance(project).createDirectory(vfsFile)
            if (directory.findFile(FILE_NAME) != null) {
                directory.deleteSingle()
            } else {
                directory.add(file)
            }
        }
    }
}

object AbcContentFile {
    fun content(): String {
        return """
           import 'dart:async';
           import 'dart:io';
           import 'package:mason/mason.dart';
           import 'package:args/args.dart';

           // To start please runscript:
           // dart run scripts/ddd.dart -d filename

           Future<void> main(List<String> args) async {
             final parser = ArgParser()
               ..addOption('dddFolder', abbr: 'd')
               ..addOption('output', abbr: 'o');
             var result = parser.parse(args);
             var name = result['dddFolder'];
             var output = result['output']??'';
             var dis = Directory.fromUri(Uri.directory(output));
             await generateFile(dddFolderNames: name, out: dis);
             exit(9);
           }

           //  url: https://gitlab.legato.co/Legato.bellamy.nguyen/abc.git
           //       path: bricks/ddd

           Future<void> generateFile({String dddFolderNames = '', Directory? out}) async {
             var toDisk = out ?? Directory.current;
             final brick = Brick.git(const GitPath(
                 'https://gitlab.legato.co/Legato.bellamy.nguyen/abc.git',
                 path: 'bricks/ddd'));
             final generator = await MasonGenerator.fromBrick(brick);
             final target = DirectoryGeneratorTarget(toDisk);
             await generator.generate(target, vars: {'filename': dddFolderNames});
             await generator.hooks.postGen(workingDirectory: target.dir.path);
           }

           
           
        """.trimIndent()
    }
}

internal class MyProjectManagerListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        project.service<MyProjectService>()
    }
}

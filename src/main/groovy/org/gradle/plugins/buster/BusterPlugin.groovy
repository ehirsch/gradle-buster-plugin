package org.gradle.plugins.buster

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.buster.config.BusterConfig
import org.gradle.plugins.buster.internal.Buster
import org.gradle.plugins.buster.internal.BusterTestingService
import org.gradle.plugins.buster.internal.Processes
import org.gradle.plugins.buster.internal.browsercapture.BrowserCapturer
import org.gradle.plugins.buster.internal.browsercapture.BrowserDriverUtil

class BusterPlugin implements Plugin<Project>{
    BusterTestingService busterService


    @Override
    void apply(Project project) {
        this.busterService = new BusterTestingService(
                browserCapturer: new BrowserCapturer(project.logger),
                project: project,
                buster: Buster.instance
        )

        project.tasks.create (BusterSetupWebDriversTask.NAME, BusterSetupWebDriversTask)
            .setDriverUtil(new BrowserDriverUtil(ant: new AntBuilder()))


        project.tasks.create (BusterTestTask.NAME, BusterTestTask)
                .setService(busterService)
                .dependsOn(BusterSetupWebDriversTask.NAME)




        Processes.instance // need to access so that shutdown hook don't go apeshit
        project.tasks.create (BusterAutoTestTask.NAME, BusterAutoTestTask)
                .setService(busterService)
                .dependsOn(BusterSetupWebDriversTask.NAME)
                .addShutdownHook {
                    busterService.tearDownAfterTest()
                }

        project.extensions.create("buster", BusterConfig, project)
    }
}

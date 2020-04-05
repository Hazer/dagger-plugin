package com.madrapps.dagger.services.impl

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.madrapps.dagger.name
import com.madrapps.dagger.orNull
import com.madrapps.dagger.services.DaggerService
import com.madrapps.dagger.services.service
import com.madrapps.dagger.toPsiClass
import com.madrapps.dagger.toPsiMethod
import com.madrapps.dagger.toolwindow.DaggerNode
import com.sun.tools.javac.code.Symbol
import dagger.model.BindingGraph
import dagger.model.DependencyRequest
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class DaggerServiceImpl(private val project: Project) : DaggerService {

    override val treeModel = DefaultTreeModel(null)

    override fun reset() {
        treeModel.setRoot(null)
        treeModel.reload()
    }

    override fun addBindings(bindingGraph: BindingGraph) {

        ApplicationManager.getApplication().runReadAction {
            val rootComponentNode = bindingGraph.rootComponentNode()
            val componentNodes = bindingGraph.componentNodes()

            val componentNode =
                DaggerNode(project, rootComponentNode.name, rootComponentNode.toPsiClass(project)!!, null)

            rootComponentNode.entryPoints().forEach {
                addNodes(it, bindingGraph, componentNode, true)
            }

            var root = project.service.treeModel.root
            if (root == null) {
                root = DefaultMutableTreeNode("")
                treeModel.setRoot(root)
            }
            (root as? DefaultMutableTreeNode)?.add(componentNode)
            treeModel.reload()
        }
    }

    private fun addNodes(
        dr: DependencyRequest,
        bindingGraph: BindingGraph,
        parentNode: DaggerNode,
        isEntryPoint: Boolean
    ) {
        var currentNode = parentNode
        val key = dr.key()
        val binding = bindingGraph.bindings(key).first()
        binding.bindingElement().ifPresent { element ->
            if (element is Symbol.MethodSymbol) {
                currentNode = addNode(dr, element, isEntryPoint)
                parentNode.add(currentNode)
            }
        }
        binding.dependencies().forEach {
            addNodes(it, bindingGraph, currentNode, false)
        }
    }

    private fun addNode(dr: DependencyRequest, element: Symbol.MethodSymbol, isEntryPoint: Boolean): DaggerNode {
        val psiMethod = element.toPsiMethod(project)!!
        val name = if (psiMethod.isConstructor) {
            psiMethod.name
        } else {
            psiMethod.returnType?.presentableText ?: "NULL"
        }
        val sourceMethod = if (isEntryPoint) {
            dr.requestElement().orNull()?.toString()
        } else null
        return DaggerNode(
            project,
            name,
            psiMethod,
            sourceMethod
        )
    }
}
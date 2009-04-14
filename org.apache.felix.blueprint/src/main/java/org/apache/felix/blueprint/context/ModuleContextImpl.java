/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.blueprint.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;
import java.net.URL;

import org.apache.felix.blueprint.HeaderParser.PathElement;
import org.apache.felix.blueprint.namespace.ComponentDefinitionRegistryImpl;
import org.apache.felix.blueprint.BlueprintConstants;
import org.apache.felix.blueprint.HeaderParser;
import org.apache.felix.blueprint.ModuleContextEventSender;
import org.apache.xbean.recipe.Repository;
import org.apache.xbean.recipe.ObjectGraph;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.blueprint.context.ModuleContext;
import org.osgi.service.blueprint.context.NoSuchComponentException;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.LocalComponentMetadata;
import org.osgi.service.blueprint.reflect.ServiceExportComponentMetadata;
import org.osgi.service.blueprint.reflect.ServiceReferenceComponentMetadata;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:dev@felix.apache.org">Apache Felix Project</a>
 * @version $Rev: 760378 $, $Date: 2009-03-31 11:31:38 +0200 (Tue, 31 Mar 2009) $
 */
public class ModuleContextImpl implements ModuleContext {

    private final BundleContext bundleContext;
    private final ModuleContextEventSender sender;
    private final List<URL> urls;
    private ComponentDefinitionRegistryImpl componentDefinitionRegistry;

    public ModuleContextImpl(BundleContext bundleContext, ModuleContextEventSender sender, List<URL> urls) {
        this.bundleContext = bundleContext;
        this.sender = sender;
        this.urls = urls;
    }

    private void checkDirectives() {
        Bundle bundle = bundleContext.getBundle();
        Dictionary headers = bundle.getHeaders();
        String symbolicName = (String)headers.get(Constants.BUNDLE_SYMBOLICNAME);
        List<PathElement> paths = HeaderParser.parseHeader(symbolicName);
        String timeout = paths.get(0).getDirective(BlueprintConstants.TIMEOUT_DIRECTIVE);
        String waitForDependencies = paths.get(0).getDirective(BlueprintConstants.WAIT_FOR_DEPENDENCIES_DIRECTIVE);

        // TODO: hook this up
        
        if (timeout != null) {
            System.out.println("Timeout: " + timeout);
        }
        if (waitForDependencies != null) {
            System.out.println("Wait-for-dependencies: " + waitForDependencies);
        }
    }
    
    public void create() {
        checkDirectives();
        sender.sendCreating(this);
        try {
            Parser parser = new Parser();
            parser.parse(urls);
            componentDefinitionRegistry = parser.getRegistry();
            Instanciator i = new Instanciator(bundleContext.getBundle());
            Repository repository = i.createRepository(componentDefinitionRegistry);
            ObjectGraph graph = new ObjectGraph(repository);
            System.out.println(graph.createAll(new ArrayList<String>(componentDefinitionRegistry.getComponentDefinitionNames())));                    
            sender.sendCreated(this);
        } catch (WaitForDependencyException e) {
            sender.sendWaiting(this, null, null); // TODO: give correct args
            // TODO: wait for dependency
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: pass the exception to the event
            sender.sendFailure(this, e);
        }
    }

    public Set<String> getComponentNames() {
        return componentDefinitionRegistry.getComponentDefinitionNames();
    }

    public Object getComponent(String name) throws NoSuchComponentException {
        ComponentMetadata metadata = getComponentMetadata(name);
        // TODO: get the component instance
        return null;
    }

    public ComponentMetadata getComponentMetadata(String name) {
        ComponentMetadata metadata = componentDefinitionRegistry.getComponentDefinition(name);
        if (metadata == null) {
            throw new NoSuchComponentException(name);
        }
        return metadata;
    }

    public Collection<ServiceReferenceComponentMetadata> getReferencedServicesMetadata() {
        return getMetadata(ServiceReferenceComponentMetadata.class);
    }

    public Collection<ServiceExportComponentMetadata> getExportedServicesMetadata() {
        return getMetadata(ServiceExportComponentMetadata.class);
    }

    public Collection<LocalComponentMetadata> getLocalComponentsMetadata() {
        return getMetadata(LocalComponentMetadata.class);
    }

    private <T> Collection<T> getMetadata(Class<T> clazz) {
        Collection<T> metadatas = new ArrayList<T>();
        for (String name : componentDefinitionRegistry.getComponentDefinitionNames()) {
            ComponentMetadata component = componentDefinitionRegistry.getComponentDefinition(name);
            if (clazz.isInstance(component)) {
                metadatas.add(clazz.cast(component));
            }
        }
        metadatas = Collections.unmodifiableCollection(metadatas);
        return metadatas;

    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void destroy() {
        sender.sendDestroying(this);
        System.out.println("Module context destroyed: " + this.bundleContext);
        // TODO: destroy all instances
        sender.sendDestroyed(this);
    }

}

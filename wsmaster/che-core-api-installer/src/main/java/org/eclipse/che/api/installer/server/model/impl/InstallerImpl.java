/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.installer.server.model.impl;

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.util.Version;
import org.eclipse.che.api.installer.server.impl.InstallerFqn;
import org.eclipse.che.api.installer.shared.model.Installer;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Anatoliy Bazko
 */
@Entity(name = "Inst")
@NamedQueries(
        {
                @NamedQuery(name = "Inst.getAll",
                            query = "SELECT i FROM Inst i"),
                @NamedQuery(name = "Inst.getAllById",
                            query = "SELECT i FROM Inst i WHERE i.id = :id"),
                @NamedQuery(name = "Inst.getByKey",
                            query = "SELECT i FROM Inst i WHERE i.id = :id AND i.version = :version"),
                @NamedQuery(name = "Inst.getTotalCount",
                            query = "SELECT COUNT(i) FROM Inst i")

        }
)
@Table(name = "installer")
public class InstallerImpl implements Installer {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    private Long internalId;

    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "dependency", nullable = false)
    @CollectionTable(name = "installer_dependencies", joinColumns = {
            @JoinColumn(name = "inst_int_id", referencedColumnName = "internal_id")})
    private List<String> dependencies;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value", nullable = false)
    @CollectionTable(name = "installer_properties", joinColumns = {@JoinColumn(name = "inst_int_id", referencedColumnName = "internal_id")})
    private Map<String, String> properties;

    @Column(name = "script")
    private String script;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "inst_int_id", referencedColumnName = "internal_id")
    @MapKeyColumn(name = "server_key")
    private Map<String, InstallerServerConfigImpl> servers;

    public InstallerImpl() {
    }

    public InstallerImpl(String id,
                         String name,
                         String version,
                         String description,
                         List<String> dependencies,
                         Map<String, String> properties,
                         String script,
                         Map<String, ? extends ServerConfig> servers) {
        Version.validate(version);

        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.dependencies = dependencies;
        this.properties = properties;
        this.script = script;
        if (servers != null) {
            this.servers = servers.entrySet()
                                  .stream()
                                  .collect(Collectors.toMap(Map.Entry::getKey,
                                                            e -> new InstallerServerConfigImpl(e.getValue())));

        }
    }

    public InstallerImpl(Installer installer) {
        this(installer.getId(),
             installer.getName(),
             installer.getVersion(),
             installer.getDescription(),
             installer.getDependencies(),
             installer.getProperties(),
             installer.getScript(),
             installer.getServers());
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        Version.validate(version);
        this.version = version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<String> getDependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public Map<String, ? extends ServerConfig> getServers() {
        if (servers == null) {
            servers = new HashMap<>();
        }
        return servers;
    }

    public void setServers(Map<String, InstallerServerConfigImpl> servers) {
        this.servers = servers;
    }

    public Long getInternalId() {
        return internalId;
    }

    public void setInternalId(Long internalId) {
        this.internalId = internalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstallerImpl)) return false;
        InstallerImpl installer = (InstallerImpl)o;
        return Objects.equals(getId(), installer.getId()) &&
               Objects.equals(getName(), installer.getName()) &&
               Objects.equals(getVersion(), installer.getVersion()) &&
               Objects.equals(getDescription(), installer.getDescription()) &&
               Objects.equals(getDependencies(), installer.getDependencies()) &&
               Objects.equals(getProperties(), installer.getProperties()) &&
               Objects.equals(getScript(), installer.getScript()) &&
               Objects.equals(getServers(), installer.getServers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(),
                            getName(),
                            getVersion(),
                            getDescription(),
                            getDependencies(),
                            getProperties(),
                            getScript(),
                            getServers());
    }

    @Override
    public String toString() {
        return "InstallerImpl{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", version='" + version + '\'' +
               ", description='" + description + '\'' +
               ", dependencies=" + dependencies +
               ", properties=" + properties +
               ", servers=" + servers +
               '}';
    }
}


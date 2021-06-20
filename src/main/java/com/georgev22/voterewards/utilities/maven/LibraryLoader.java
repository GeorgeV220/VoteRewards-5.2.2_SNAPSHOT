/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.georgev22.voterewards.utilities.maven;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Resolves {@link MavenLibrary} annotations for a class, and loads the dependency
 * into the classloader.
 *
 * @deprecated Probably will be removed in feature release
 */
@NotNull
@Deprecated
public final record LibraryLoader(Plugin plugin) {

    private static final Method ADD_URL_METHOD;

    static {
        try {
            ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolves all {@link MavenLibrary} annotations on the given object.
     *
     * @param object the object to load libraries for.
     */
    public void loadAll(Object object) {
        loadAll(object.getClass());
    }

    /**
     * Resolves all {@link MavenLibrary} annotations on the Class that extends {@link Plugin}.
     *
     * @see #LibraryLoader(Plugin)
     */
    public void loadAll() {
        if (plugin == null) {
            throw new RuntimeException("Plugin is null!");
        }
        loadAll(plugin.getClass());
    }

    /**
     * Resolves all {@link MavenLibrary} annotations on the given class.
     *
     * @param clazz the class to load libraries for.
     */
    public void loadAll(Class<?> clazz) {
        MavenLibrary[] libs = clazz.getDeclaredAnnotationsByType(MavenLibrary.class);
        if (libs == null) {
            return;
        }

        for (MavenLibrary lib : libs) {
            load(lib.groupId(), lib.artifactId(), lib.version(), lib.repo().url());
        }
    }

    public void load(String groupId, String artifactId, String version, String repoUrl) {
        load(new Dependency(groupId, artifactId, version, repoUrl));
    }

    public void load(Dependency d) {
        Bukkit.getLogger().info(String.format("[" + plugin.getName() + "] Loading dependency %s:%s:%s from %s", d.groupId(), d.artifactId(), d.version(), d.repoUrl()));

        for (File file : getLibFolder().listFiles((dir, name) -> name.endsWith(".jar"))) {
            String[] fileandversion = file.getName().replace(".jar", "").split("_");
            String dependencyName = fileandversion[0];
            String version = fileandversion[1];
            if (d.artifactId().equalsIgnoreCase(dependencyName)) {
                if (!d.version().equalsIgnoreCase(version)) {
                    Bukkit.getLogger().info("[" + plugin.getName() + "] An old version of the dependency exists. Attempting to delete...");
                    if (file.delete()) {
                        Bukkit.getLogger().info("[" + plugin.getName() + "] Dependency '" + dependencyName + "' with version '" + version + "' has been deleted!\nA new version will be downloaded.");
                    }
                }
            }
        }

        String name = d.artifactId() + "_" + d.version();

        File saveLocation = new File(getLibFolder(), name + ".jar");
        if (!saveLocation.exists()) {

            try {
                Bukkit.getLogger().info("[" + plugin.getName() + "] Dependency '" + name + "' is not already in the libraries folder. Attempting to download...");
                URL url = d.url();

                try (InputStream is = url.openStream()) {
                    Files.copy(is, saveLocation.toPath());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Bukkit.getLogger().info("[" + plugin.getName() + "] Dependency '" + name + "' successfully downloaded.");
        }

        if (!saveLocation.exists()) {
            throw new RuntimeException("[" + plugin.getName() + "] Unable to download dependency: " + d);
        }

        URLClassLoader classLoader = (URLClassLoader) plugin.getClass().getClassLoader();
        try {
            ADD_URL_METHOD.invoke(classLoader, saveLocation.toURI().toURL());
        } catch (Exception e) {
            throw new RuntimeException("[" + plugin.getName() + "] Unable to load dependency: " + saveLocation, e);
        }

        Bukkit.getLogger().info("[" + plugin.getName() + "] Loaded dependency '" + name + "' successfully.");
    }

    private File getLibFolder() {
        File libs = new File(plugin.getDataFolder(), "libraries");
        if (libs.mkdirs()) {
            Bukkit.getLogger().info("[" + plugin.getName() + "] libraries folder created!");
        }
        return libs;
    }

    @NotNull
    private record Dependency(String groupId, String artifactId, String version,
                              String repoUrl) {
        private Dependency(String groupId, String artifactId, String version, String repoUrl) {
            this.groupId = Objects.requireNonNull(groupId, "groupId");
            this.artifactId = Objects.requireNonNull(artifactId, "artifactId");
            this.version = Objects.requireNonNull(version, "version");
            this.repoUrl = Objects.requireNonNull(repoUrl, "repoUrl");
        }

        public URL url() throws MalformedURLException {
            String repo = this.repoUrl;
            if (!repo.endsWith("/")) {
                repo += "/";
            }
            repo += "%s/%s/%s/%s-%s.jar";

            String url = String.format(repo, this.groupId.replace(".", "/"), this.artifactId, this.version, this.artifactId, this.version);
            return new URL(url);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof final Dependency other)) return false;
            return this.groupId().equals(other.groupId()) &&
                    this.artifactId().equals(other.artifactId()) &&
                    this.version().equals(other.version()) &&
                    this.repoUrl().equals(other.repoUrl());
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.groupId().hashCode();
            result = result * PRIME + this.artifactId().hashCode();
            result = result * PRIME + this.version().hashCode();
            result = result * PRIME + this.repoUrl().hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "LibraryLoader.Dependency(" +
                    "groupId=" + this.groupId() + ", " +
                    "artifactId=" + this.artifactId() + ", " +
                    "version=" + this.version() + ", " +
                    "repoUrl=" + this.repoUrl() + ")";
        }
    }


}
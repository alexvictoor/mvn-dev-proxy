package com.github.alexvictoor.proxy;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FileSystemRouteTest {

    @Test
    public void should_create_route() throws Exception {
        // when
        FileSystemRoute route = FileSystemRoute.create("webjar", ".");
        // then
        assertThat(route).isNotNull();
    }

    @Test( expected = Exception.class)
    public void should_fail_creating_route_for_incorrect_fs_path() throws Exception {
        FileSystemRoute.create("webjar", "./bad");
    }

    @Test
    public void should_find_file_for_prefixed_uri() {
        // given
        FileSystemRoute route = FileSystemRoute.create("webjar", ".");
        // when
        File file = route.findFile("webjar/README.md");
        // then
        assertThat(file)
                .isNotNull()
                .exists()
                .hasName("README.md");
    }

    @Test
    public void should_return_null_for_not_prefixed_uri() {
        // given
        FileSystemRoute route = FileSystemRoute.create("webjar", ".");
        // when
        File file = route.findFile("bad.html");
        // then
        assertThat(file).isNull();
    }

}
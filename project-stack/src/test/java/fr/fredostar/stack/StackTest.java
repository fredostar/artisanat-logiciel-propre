package fr.fredostar.stack;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StackTest {

    @Test
    public void canCreateStack() throws Exception {
        Stack stack = new Stack();
        assertThat(stack.isEmpty()).isFalse();
    }
}

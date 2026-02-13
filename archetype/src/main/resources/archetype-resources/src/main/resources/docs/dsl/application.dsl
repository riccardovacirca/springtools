application {
  paradigm        = "explicit-imperative"
  primary_language = "java"
  mental_model    = "C-like"
}

code_style {
  philosophy = "readability-through-explicitness"

  variables {
    declaration_assignment = "separated"

    pattern {
      // REQUIRED: Separate declaration from assignment
      // This makes the code more readable and debuggable

      allowed {
        Type variable;
        Type other;

        variable = expression();
        other = builder().chain().build();

        return variable;
      }

      forbidden {
        Type variable = expression();  // inline initialization
        return expression();           // inline return
      }
    }

    rationale {
      // 1. Each line has single responsibility
      // 2. Breakpoint-friendly debugging
      // 3. Clear variable lifecycle visibility
      // 4. Easier to refactor and modify
    }
  }

  formatting {
    indentation = "2-spaces"
    line_length = "120-characters"

    braces {
      methods      = "next-line"     // Allman/BSD style
      blocks       = "end-of-line"   // K&R style for if/for/while
    }
  }
}

spring {
  role = "infrastructure-only"

  allowed {
    datasource
    configuration
    lifecycle
    dependency_injection (constructor_only)
  }

  forbidden {
    aop
    proxy_generation
    runtime_bytecode_enhancement
    implicit_transactions
    implicit_thread_binding
  }
}

annotations {
  allowed {
    @Configuration
    @Bean
    @Component (optional)

    // Spring MVC infrastructure (controller layer only)
    @RestController
    @Controller
    @RequestMapping
    @GetMapping
    @PostMapping
    @PutMapping
    @DeleteMapping
    @PatchMapping

    // Parameter binding (controller layer only)
    @RequestBody
    @RequestParam
    @PathVariable
    @RequestHeader

    // Validation (DTOs and controller parameters)
    @Valid
    @NotNull
    @NotBlank
    @NotEmpty
    @Size
    @Min
    @Max
    @Email
    @Pattern
    @Positive
    @PositiveOrZero
    @Negative
    @NegativeOrZero
  }

  forbidden {
    @Transactional
    @Autowired (field)
    @Async
    @Cacheable
    @Repository
    @Service
    @ControllerAdvice
  }
}

dependency_injection {
  style = "constructor"
  rules {
    no_reflection
    no_field_injection
    no_hidden_lifecycle
  }
}

threading {
  model = "explicit"

  rules {
    no_shared_mutable_state
    thread_local_allowed
    singleton_stateless_only
  }
}

database {
  access_model = "jdbc-direct"

  connection {
    source      = "DataSource"
    scope       = "thread"
    management  = "manual"
  }

  transactions {
    mode = "explicit"

    primitives {
      begin
      commit
      rollback
    }

    forbidden {
      declarative
      implicit
    }
  }

  abstractions {
    forbid {
      JdbcTemplate
      ORM
      entity_manager
    }
  }
}

services {
  nature = "plain-java"

  rules {
    no_annotations_required
    no_framework_dependency_in_logic
    deterministic_behavior
  }
}

web {
  controllers {
    role = "adapter-layer"

    allowed {
      parameter_binding
      response_serialization
    }

    forbidden {
      business_logic
      transactions
    }
  }
}

error_handling {
  strategy = "explicit"

  rules {
    no_silent_conversion
    no_magic_mapping
    checked_exceptions_allowed
  }
}

documentation {
  purpose = "contractual"

  guarantees {
    no_hidden_control_flow
    debuggable_with_breakpoints
    behavior_readable_from_code
  }
}

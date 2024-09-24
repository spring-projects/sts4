// Generated from PostgreSqlParser.g4 by ANTLR 4.13.1
package org.springframework.ide.vscode.parser.postgresql;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PostgreSqlParser}.
 */
public interface PostgreSqlParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#root}.
	 * @param ctx the parse tree
	 */
	void enterRoot(PostgreSqlParser.RootContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#root}.
	 * @param ctx the parse tree
	 */
	void exitRoot(PostgreSqlParser.RootContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(PostgreSqlParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(PostgreSqlParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#abort_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAbort_stmt(PostgreSqlParser.Abort_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#abort_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAbort_stmt(PostgreSqlParser.Abort_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_stmt(PostgreSqlParser.Alter_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_stmt(PostgreSqlParser.Alter_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_aggregate_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_aggregate_stmt(PostgreSqlParser.Alter_aggregate_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_aggregate_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_aggregate_stmt(PostgreSqlParser.Alter_aggregate_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_collation_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_collation_stmt(PostgreSqlParser.Alter_collation_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_collation_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_collation_stmt(PostgreSqlParser.Alter_collation_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_conversion_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_conversion_stmt(PostgreSqlParser.Alter_conversion_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_conversion_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_conversion_stmt(PostgreSqlParser.Alter_conversion_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_stmt(PostgreSqlParser.Alter_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_stmt(PostgreSqlParser.Alter_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_default_privileges_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_default_privileges_stmt(PostgreSqlParser.Alter_default_privileges_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_default_privileges_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_default_privileges_stmt(PostgreSqlParser.Alter_default_privileges_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_domain_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_domain_stmt(PostgreSqlParser.Alter_domain_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_domain_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_domain_stmt(PostgreSqlParser.Alter_domain_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_event_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_event_trigger_stmt(PostgreSqlParser.Alter_event_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_event_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_event_trigger_stmt(PostgreSqlParser.Alter_event_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_extension_stmt(PostgreSqlParser.Alter_extension_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_extension_stmt(PostgreSqlParser.Alter_extension_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_foreign_data_wrapper_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_foreign_data_wrapper_stmt(PostgreSqlParser.Alter_foreign_data_wrapper_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_foreign_data_wrapper_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_foreign_data_wrapper_stmt(PostgreSqlParser.Alter_foreign_data_wrapper_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_foreign_table_action}.
	 * @param ctx the parse tree
	 */
	void enterAlter_foreign_table_action(PostgreSqlParser.Alter_foreign_table_actionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_foreign_table_action}.
	 * @param ctx the parse tree
	 */
	void exitAlter_foreign_table_action(PostgreSqlParser.Alter_foreign_table_actionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_foreign_table_action_list}.
	 * @param ctx the parse tree
	 */
	void enterAlter_foreign_table_action_list(PostgreSqlParser.Alter_foreign_table_action_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_foreign_table_action_list}.
	 * @param ctx the parse tree
	 */
	void exitAlter_foreign_table_action_list(PostgreSqlParser.Alter_foreign_table_action_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_foreign_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_foreign_table_stmt(PostgreSqlParser.Alter_foreign_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_foreign_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_foreign_table_stmt(PostgreSqlParser.Alter_foreign_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_function_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_function_stmt(PostgreSqlParser.Alter_function_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_function_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_function_stmt(PostgreSqlParser.Alter_function_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_group_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_group_stmt(PostgreSqlParser.Alter_group_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_group_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_group_stmt(PostgreSqlParser.Alter_group_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_index_stmt(PostgreSqlParser.Alter_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_index_stmt(PostgreSqlParser.Alter_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_language_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_language_stmt(PostgreSqlParser.Alter_language_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_language_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_language_stmt(PostgreSqlParser.Alter_language_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_large_object_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_large_object_stmt(PostgreSqlParser.Alter_large_object_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_large_object_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_large_object_stmt(PostgreSqlParser.Alter_large_object_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_materialize_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_materialize_view_stmt(PostgreSqlParser.Alter_materialize_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_materialize_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_materialize_view_stmt(PostgreSqlParser.Alter_materialize_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_operator_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_operator_stmt(PostgreSqlParser.Alter_operator_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_operator_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_operator_stmt(PostgreSqlParser.Alter_operator_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_operator_class_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_operator_class_stmt(PostgreSqlParser.Alter_operator_class_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_operator_class_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_operator_class_stmt(PostgreSqlParser.Alter_operator_class_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_operator_family_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_operator_family_stmt(PostgreSqlParser.Alter_operator_family_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_operator_family_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_operator_family_stmt(PostgreSqlParser.Alter_operator_family_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_policy_stmt(PostgreSqlParser.Alter_policy_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_policy_stmt(PostgreSqlParser.Alter_policy_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_publication_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_publication_stmt(PostgreSqlParser.Alter_publication_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_publication_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_publication_stmt(PostgreSqlParser.Alter_publication_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_role_options}.
	 * @param ctx the parse tree
	 */
	void enterAlter_role_options(PostgreSqlParser.Alter_role_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_role_options}.
	 * @param ctx the parse tree
	 */
	void exitAlter_role_options(PostgreSqlParser.Alter_role_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_role_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_role_stmt(PostgreSqlParser.Alter_role_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_role_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_role_stmt(PostgreSqlParser.Alter_role_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_rule_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_rule_stmt(PostgreSqlParser.Alter_rule_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_rule_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_rule_stmt(PostgreSqlParser.Alter_rule_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_schema_stmt(PostgreSqlParser.Alter_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_schema_stmt(PostgreSqlParser.Alter_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_sequence_stmt(PostgreSqlParser.Alter_sequence_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_sequence_stmt(PostgreSqlParser.Alter_sequence_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_server_options_list}.
	 * @param ctx the parse tree
	 */
	void enterAlter_server_options_list(PostgreSqlParser.Alter_server_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_server_options_list}.
	 * @param ctx the parse tree
	 */
	void exitAlter_server_options_list(PostgreSqlParser.Alter_server_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_server_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_server_stmt(PostgreSqlParser.Alter_server_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_server_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_server_stmt(PostgreSqlParser.Alter_server_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_statistics_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_statistics_stmt(PostgreSqlParser.Alter_statistics_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_statistics_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_statistics_stmt(PostgreSqlParser.Alter_statistics_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_subscription_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_subscription_stmt(PostgreSqlParser.Alter_subscription_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_subscription_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_subscription_stmt(PostgreSqlParser.Alter_subscription_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_system_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_system_stmt(PostgreSqlParser.Alter_system_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_system_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_system_stmt(PostgreSqlParser.Alter_system_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_stmt(PostgreSqlParser.Alter_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_stmt(PostgreSqlParser.Alter_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_tablespace_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_tablespace_stmt(PostgreSqlParser.Alter_tablespace_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_tablespace_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_tablespace_stmt(PostgreSqlParser.Alter_tablespace_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_text_search_config_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_text_search_config_stmt(PostgreSqlParser.Alter_text_search_config_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_text_search_config_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_text_search_config_stmt(PostgreSqlParser.Alter_text_search_config_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_text_search_dict_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_text_search_dict_stmt(PostgreSqlParser.Alter_text_search_dict_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_text_search_dict_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_text_search_dict_stmt(PostgreSqlParser.Alter_text_search_dict_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_text_search_parser_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_text_search_parser_stmt(PostgreSqlParser.Alter_text_search_parser_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_text_search_parser_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_text_search_parser_stmt(PostgreSqlParser.Alter_text_search_parser_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_text_search_template_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_text_search_template_stmt(PostgreSqlParser.Alter_text_search_template_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_text_search_template_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_text_search_template_stmt(PostgreSqlParser.Alter_text_search_template_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_trigger_stmt(PostgreSqlParser.Alter_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_trigger_stmt(PostgreSqlParser.Alter_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_type_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_type_stmt(PostgreSqlParser.Alter_type_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_type_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_type_stmt(PostgreSqlParser.Alter_type_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_user_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_user_stmt(PostgreSqlParser.Alter_user_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_user_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_user_stmt(PostgreSqlParser.Alter_user_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_user_mapping_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_user_mapping_stmt(PostgreSqlParser.Alter_user_mapping_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_user_mapping_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_user_mapping_stmt(PostgreSqlParser.Alter_user_mapping_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alter_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_view_stmt(PostgreSqlParser.Alter_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alter_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_view_stmt(PostgreSqlParser.Alter_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAnalyze_stmt(PostgreSqlParser.Analyze_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAnalyze_stmt(PostgreSqlParser.Analyze_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#close_stmt}.
	 * @param ctx the parse tree
	 */
	void enterClose_stmt(PostgreSqlParser.Close_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#close_stmt}.
	 * @param ctx the parse tree
	 */
	void exitClose_stmt(PostgreSqlParser.Close_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#cluster_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCluster_stmt(PostgreSqlParser.Cluster_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#cluster_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCluster_stmt(PostgreSqlParser.Cluster_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#comment_stmt}.
	 * @param ctx the parse tree
	 */
	void enterComment_stmt(PostgreSqlParser.Comment_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#comment_stmt}.
	 * @param ctx the parse tree
	 */
	void exitComment_stmt(PostgreSqlParser.Comment_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCommit_stmt(PostgreSqlParser.Commit_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCommit_stmt(PostgreSqlParser.Commit_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#commit_prepared_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCommit_prepared_stmt(PostgreSqlParser.Commit_prepared_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#commit_prepared_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCommit_prepared_stmt(PostgreSqlParser.Commit_prepared_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#copy_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCopy_stmt(PostgreSqlParser.Copy_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#copy_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCopy_stmt(PostgreSqlParser.Copy_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_stmt(PostgreSqlParser.Create_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_stmt(PostgreSqlParser.Create_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_access_method_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_access_method_stmt(PostgreSqlParser.Create_access_method_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_access_method_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_access_method_stmt(PostgreSqlParser.Create_access_method_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_aggregate_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_aggregate_stmt(PostgreSqlParser.Create_aggregate_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_aggregate_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_aggregate_stmt(PostgreSqlParser.Create_aggregate_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_cast_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_cast_stmt(PostgreSqlParser.Create_cast_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_cast_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_cast_stmt(PostgreSqlParser.Create_cast_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_collation_opt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_collation_opt(PostgreSqlParser.Create_collation_optContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_collation_opt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_collation_opt(PostgreSqlParser.Create_collation_optContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_collation_opt_list}.
	 * @param ctx the parse tree
	 */
	void enterCreate_collation_opt_list(PostgreSqlParser.Create_collation_opt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_collation_opt_list}.
	 * @param ctx the parse tree
	 */
	void exitCreate_collation_opt_list(PostgreSqlParser.Create_collation_opt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_collation_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_collation_stmt(PostgreSqlParser.Create_collation_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_collation_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_collation_stmt(PostgreSqlParser.Create_collation_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_conversion_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_conversion_stmt(PostgreSqlParser.Create_conversion_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_conversion_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_conversion_stmt(PostgreSqlParser.Create_conversion_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_database_stmt(PostgreSqlParser.Create_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_database_stmt(PostgreSqlParser.Create_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#domain_constraint}.
	 * @param ctx the parse tree
	 */
	void enterDomain_constraint(PostgreSqlParser.Domain_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#domain_constraint}.
	 * @param ctx the parse tree
	 */
	void exitDomain_constraint(PostgreSqlParser.Domain_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_domain_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_domain_stmt(PostgreSqlParser.Create_domain_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_domain_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_domain_stmt(PostgreSqlParser.Create_domain_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_event_trigger_cond}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_trigger_cond(PostgreSqlParser.Create_event_trigger_condContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_event_trigger_cond}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_trigger_cond(PostgreSqlParser.Create_event_trigger_condContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_event_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_trigger_stmt(PostgreSqlParser.Create_event_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_event_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_trigger_stmt(PostgreSqlParser.Create_event_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_foreign_data_options}.
	 * @param ctx the parse tree
	 */
	void enterCreate_foreign_data_options(PostgreSqlParser.Create_foreign_data_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_foreign_data_options}.
	 * @param ctx the parse tree
	 */
	void exitCreate_foreign_data_options(PostgreSqlParser.Create_foreign_data_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_foreign_data_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_foreign_data_stmt(PostgreSqlParser.Create_foreign_data_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_foreign_data_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_foreign_data_stmt(PostgreSqlParser.Create_foreign_data_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_foreign_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_foreign_table_stmt(PostgreSqlParser.Create_foreign_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_foreign_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_foreign_table_stmt(PostgreSqlParser.Create_foreign_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_function_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function_stmt(PostgreSqlParser.Create_function_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_function_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function_stmt(PostgreSqlParser.Create_function_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_group_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_group_stmt(PostgreSqlParser.Create_group_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_group_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_group_stmt(PostgreSqlParser.Create_group_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index_stmt(PostgreSqlParser.Create_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index_stmt(PostgreSqlParser.Create_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_language_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_language_stmt(PostgreSqlParser.Create_language_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_language_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_language_stmt(PostgreSqlParser.Create_language_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_materialized_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_materialized_view_stmt(PostgreSqlParser.Create_materialized_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_materialized_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_materialized_view_stmt(PostgreSqlParser.Create_materialized_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_operator_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_operator_stmt(PostgreSqlParser.Create_operator_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_operator_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_operator_stmt(PostgreSqlParser.Create_operator_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_operator_class_opt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_operator_class_opt(PostgreSqlParser.Create_operator_class_optContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_operator_class_opt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_operator_class_opt(PostgreSqlParser.Create_operator_class_optContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_operator_class_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_operator_class_stmt(PostgreSqlParser.Create_operator_class_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_operator_class_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_operator_class_stmt(PostgreSqlParser.Create_operator_class_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_operator_family_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_operator_family_stmt(PostgreSqlParser.Create_operator_family_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_operator_family_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_operator_family_stmt(PostgreSqlParser.Create_operator_family_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_policy_stmt(PostgreSqlParser.Create_policy_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_policy_stmt(PostgreSqlParser.Create_policy_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_role_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_role_stmt(PostgreSqlParser.Create_role_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_role_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_role_stmt(PostgreSqlParser.Create_role_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_rule_event}.
	 * @param ctx the parse tree
	 */
	void enterCreate_rule_event(PostgreSqlParser.Create_rule_eventContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_rule_event}.
	 * @param ctx the parse tree
	 */
	void exitCreate_rule_event(PostgreSqlParser.Create_rule_eventContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_rule_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_rule_stmt(PostgreSqlParser.Create_rule_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_rule_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_rule_stmt(PostgreSqlParser.Create_rule_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_schema_stmt(PostgreSqlParser.Create_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_schema_stmt(PostgreSqlParser.Create_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_sequence_stmt(PostgreSqlParser.Create_sequence_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_sequence_stmt(PostgreSqlParser.Create_sequence_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_server_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_server_stmt(PostgreSqlParser.Create_server_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_server_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_server_stmt(PostgreSqlParser.Create_server_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_statistics_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_statistics_stmt(PostgreSqlParser.Create_statistics_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_statistics_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_statistics_stmt(PostgreSqlParser.Create_statistics_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_subscription_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_subscription_stmt(PostgreSqlParser.Create_subscription_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_subscription_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_subscription_stmt(PostgreSqlParser.Create_subscription_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_stmt(PostgreSqlParser.Create_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_stmt(PostgreSqlParser.Create_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_table_as_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_as_stmt(PostgreSqlParser.Create_table_as_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_table_as_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_as_stmt(PostgreSqlParser.Create_table_as_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_tablespace_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_tablespace_stmt(PostgreSqlParser.Create_tablespace_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_tablespace_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_tablespace_stmt(PostgreSqlParser.Create_tablespace_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_text_search_config_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_text_search_config_stmt(PostgreSqlParser.Create_text_search_config_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_text_search_config_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_text_search_config_stmt(PostgreSqlParser.Create_text_search_config_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_text_search_dict_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_text_search_dict_stmt(PostgreSqlParser.Create_text_search_dict_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_text_search_dict_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_text_search_dict_stmt(PostgreSqlParser.Create_text_search_dict_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_text_search_parser_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_text_search_parser_stmt(PostgreSqlParser.Create_text_search_parser_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_text_search_parser_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_text_search_parser_stmt(PostgreSqlParser.Create_text_search_parser_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_text_search_template_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_text_search_template_stmt(PostgreSqlParser.Create_text_search_template_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_text_search_template_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_text_search_template_stmt(PostgreSqlParser.Create_text_search_template_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_transform_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_transform_stmt(PostgreSqlParser.Create_transform_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_transform_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_transform_stmt(PostgreSqlParser.Create_transform_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_trigger_stmt(PostgreSqlParser.Create_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_trigger_stmt(PostgreSqlParser.Create_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_type_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type_stmt(PostgreSqlParser.Create_type_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_type_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type_stmt(PostgreSqlParser.Create_type_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_user_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_user_stmt(PostgreSqlParser.Create_user_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_user_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_user_stmt(PostgreSqlParser.Create_user_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_user_mapping_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_user_mapping_stmt(PostgreSqlParser.Create_user_mapping_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_user_mapping_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_user_mapping_stmt(PostgreSqlParser.Create_user_mapping_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view_stmt(PostgreSqlParser.Create_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view_stmt(PostgreSqlParser.Create_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#deallocate_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDeallocate_stmt(PostgreSqlParser.Deallocate_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#deallocate_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDeallocate_stmt(PostgreSqlParser.Deallocate_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#declare_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDeclare_stmt(PostgreSqlParser.Declare_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#declare_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDeclare_stmt(PostgreSqlParser.Declare_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDelete_stmt(PostgreSqlParser.Delete_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDelete_stmt(PostgreSqlParser.Delete_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#discard_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDiscard_stmt(PostgreSqlParser.Discard_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#discard_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDiscard_stmt(PostgreSqlParser.Discard_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_stmt(PostgreSqlParser.Drop_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_stmt(PostgreSqlParser.Drop_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_access_method_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_access_method_stmt(PostgreSqlParser.Drop_access_method_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_access_method_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_access_method_stmt(PostgreSqlParser.Drop_access_method_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_aggregate_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_aggregate_stmt(PostgreSqlParser.Drop_aggregate_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_aggregate_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_aggregate_stmt(PostgreSqlParser.Drop_aggregate_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_cast_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_cast_stmt(PostgreSqlParser.Drop_cast_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_cast_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_cast_stmt(PostgreSqlParser.Drop_cast_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_collation_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_collation_stmt(PostgreSqlParser.Drop_collation_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_collation_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_collation_stmt(PostgreSqlParser.Drop_collation_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_conversion_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_conversion_stmt(PostgreSqlParser.Drop_conversion_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_conversion_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_conversion_stmt(PostgreSqlParser.Drop_conversion_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_database_stmt(PostgreSqlParser.Drop_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_database_stmt(PostgreSqlParser.Drop_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_domain_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_domain_stmt(PostgreSqlParser.Drop_domain_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_domain_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_domain_stmt(PostgreSqlParser.Drop_domain_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_event_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_event_trigger_stmt(PostgreSqlParser.Drop_event_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_event_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_event_trigger_stmt(PostgreSqlParser.Drop_event_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_extension_stmt(PostgreSqlParser.Drop_extension_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_extension_stmt(PostgreSqlParser.Drop_extension_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_foreign_data_wrapper_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_foreign_data_wrapper_stmt(PostgreSqlParser.Drop_foreign_data_wrapper_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_foreign_data_wrapper_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_foreign_data_wrapper_stmt(PostgreSqlParser.Drop_foreign_data_wrapper_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_foreign_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_foreign_table_stmt(PostgreSqlParser.Drop_foreign_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_foreign_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_foreign_table_stmt(PostgreSqlParser.Drop_foreign_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_function_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_function_stmt(PostgreSqlParser.Drop_function_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_function_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_function_stmt(PostgreSqlParser.Drop_function_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_group_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_group_stmt(PostgreSqlParser.Drop_group_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_group_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_group_stmt(PostgreSqlParser.Drop_group_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_index_stmt(PostgreSqlParser.Drop_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_index_stmt(PostgreSqlParser.Drop_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_language_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_language_stmt(PostgreSqlParser.Drop_language_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_language_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_language_stmt(PostgreSqlParser.Drop_language_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_materialized_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_materialized_view_stmt(PostgreSqlParser.Drop_materialized_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_materialized_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_materialized_view_stmt(PostgreSqlParser.Drop_materialized_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_operator_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_operator_stmt(PostgreSqlParser.Drop_operator_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_operator_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_operator_stmt(PostgreSqlParser.Drop_operator_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_operator_class_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_operator_class_stmt(PostgreSqlParser.Drop_operator_class_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_operator_class_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_operator_class_stmt(PostgreSqlParser.Drop_operator_class_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_operator_family_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_operator_family_stmt(PostgreSqlParser.Drop_operator_family_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_operator_family_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_operator_family_stmt(PostgreSqlParser.Drop_operator_family_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_owned_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_owned_stmt(PostgreSqlParser.Drop_owned_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_owned_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_owned_stmt(PostgreSqlParser.Drop_owned_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_policy_stmt(PostgreSqlParser.Drop_policy_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_policy_stmt(PostgreSqlParser.Drop_policy_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_publication_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_publication_stmt(PostgreSqlParser.Drop_publication_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_publication_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_publication_stmt(PostgreSqlParser.Drop_publication_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_role_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_role_stmt(PostgreSqlParser.Drop_role_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_role_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_role_stmt(PostgreSqlParser.Drop_role_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_rule_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_rule_stmt(PostgreSqlParser.Drop_rule_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_rule_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_rule_stmt(PostgreSqlParser.Drop_rule_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_schema_stmt(PostgreSqlParser.Drop_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_schema_stmt(PostgreSqlParser.Drop_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_sequence_stmt(PostgreSqlParser.Drop_sequence_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_sequence_stmt(PostgreSqlParser.Drop_sequence_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_server_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_server_stmt(PostgreSqlParser.Drop_server_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_server_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_server_stmt(PostgreSqlParser.Drop_server_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_statistics_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_statistics_stmt(PostgreSqlParser.Drop_statistics_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_statistics_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_statistics_stmt(PostgreSqlParser.Drop_statistics_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_subscription_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_subscription_stmt(PostgreSqlParser.Drop_subscription_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_subscription_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_subscription_stmt(PostgreSqlParser.Drop_subscription_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table_stmt(PostgreSqlParser.Drop_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table_stmt(PostgreSqlParser.Drop_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_tablespace_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_tablespace_stmt(PostgreSqlParser.Drop_tablespace_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_tablespace_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_tablespace_stmt(PostgreSqlParser.Drop_tablespace_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_text_search_config_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_text_search_config_stmt(PostgreSqlParser.Drop_text_search_config_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_text_search_config_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_text_search_config_stmt(PostgreSqlParser.Drop_text_search_config_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_text_search_dict_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_text_search_dict_stmt(PostgreSqlParser.Drop_text_search_dict_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_text_search_dict_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_text_search_dict_stmt(PostgreSqlParser.Drop_text_search_dict_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_text_search_parser_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_text_search_parser_stmt(PostgreSqlParser.Drop_text_search_parser_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_text_search_parser_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_text_search_parser_stmt(PostgreSqlParser.Drop_text_search_parser_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_text_search_template_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_text_search_template_stmt(PostgreSqlParser.Drop_text_search_template_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_text_search_template_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_text_search_template_stmt(PostgreSqlParser.Drop_text_search_template_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_transform_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_transform_stmt(PostgreSqlParser.Drop_transform_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_transform_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_transform_stmt(PostgreSqlParser.Drop_transform_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_trigger_stmt(PostgreSqlParser.Drop_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_trigger_stmt(PostgreSqlParser.Drop_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_type_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_type_stmt(PostgreSqlParser.Drop_type_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_type_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_type_stmt(PostgreSqlParser.Drop_type_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_user_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_user_stmt(PostgreSqlParser.Drop_user_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_user_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_user_stmt(PostgreSqlParser.Drop_user_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_user_mapping_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_user_mapping_stmt(PostgreSqlParser.Drop_user_mapping_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_user_mapping_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_user_mapping_stmt(PostgreSqlParser.Drop_user_mapping_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_view_stmt(PostgreSqlParser.Drop_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_view_stmt(PostgreSqlParser.Drop_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#execute_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExecute_stmt(PostgreSqlParser.Execute_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#execute_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExecute_stmt(PostgreSqlParser.Execute_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#explain_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExplain_stmt(PostgreSqlParser.Explain_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#explain_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExplain_stmt(PostgreSqlParser.Explain_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#fetch_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFetch_stmt(PostgreSqlParser.Fetch_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#fetch_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFetch_stmt(PostgreSqlParser.Fetch_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#grant_stmt}.
	 * @param ctx the parse tree
	 */
	void enterGrant_stmt(PostgreSqlParser.Grant_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#grant_stmt}.
	 * @param ctx the parse tree
	 */
	void exitGrant_stmt(PostgreSqlParser.Grant_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#import_foreign_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterImport_foreign_schema_stmt(PostgreSqlParser.Import_foreign_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#import_foreign_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitImport_foreign_schema_stmt(PostgreSqlParser.Import_foreign_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void enterInsert_stmt(PostgreSqlParser.Insert_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void exitInsert_stmt(PostgreSqlParser.Insert_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#listen_stmt}.
	 * @param ctx the parse tree
	 */
	void enterListen_stmt(PostgreSqlParser.Listen_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#listen_stmt}.
	 * @param ctx the parse tree
	 */
	void exitListen_stmt(PostgreSqlParser.Listen_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#load_stmt}.
	 * @param ctx the parse tree
	 */
	void enterLoad_stmt(PostgreSqlParser.Load_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#load_stmt}.
	 * @param ctx the parse tree
	 */
	void exitLoad_stmt(PostgreSqlParser.Load_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#lock_stmt}.
	 * @param ctx the parse tree
	 */
	void enterLock_stmt(PostgreSqlParser.Lock_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#lock_stmt}.
	 * @param ctx the parse tree
	 */
	void exitLock_stmt(PostgreSqlParser.Lock_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#move_stmt}.
	 * @param ctx the parse tree
	 */
	void enterMove_stmt(PostgreSqlParser.Move_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#move_stmt}.
	 * @param ctx the parse tree
	 */
	void exitMove_stmt(PostgreSqlParser.Move_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#notify_stmt}.
	 * @param ctx the parse tree
	 */
	void enterNotify_stmt(PostgreSqlParser.Notify_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#notify_stmt}.
	 * @param ctx the parse tree
	 */
	void exitNotify_stmt(PostgreSqlParser.Notify_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#prepare_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPrepare_stmt(PostgreSqlParser.Prepare_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#prepare_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPrepare_stmt(PostgreSqlParser.Prepare_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#prepare_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPrepare_transaction_stmt(PostgreSqlParser.Prepare_transaction_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#prepare_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPrepare_transaction_stmt(PostgreSqlParser.Prepare_transaction_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#reassign_owned_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReassign_owned_stmt(PostgreSqlParser.Reassign_owned_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#reassign_owned_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReassign_owned_stmt(PostgreSqlParser.Reassign_owned_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#refresh_materialized_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRefresh_materialized_view_stmt(PostgreSqlParser.Refresh_materialized_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#refresh_materialized_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRefresh_materialized_view_stmt(PostgreSqlParser.Refresh_materialized_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#reindex_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReindex_stmt(PostgreSqlParser.Reindex_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#reindex_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReindex_stmt(PostgreSqlParser.Reindex_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#release_savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRelease_savepoint_stmt(PostgreSqlParser.Release_savepoint_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#release_savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRelease_savepoint_stmt(PostgreSqlParser.Release_savepoint_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#reset_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReset_stmt(PostgreSqlParser.Reset_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#reset_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReset_stmt(PostgreSqlParser.Reset_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#revoke_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_stmt(PostgreSqlParser.Revoke_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#revoke_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_stmt(PostgreSqlParser.Revoke_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRollback_stmt(PostgreSqlParser.Rollback_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRollback_stmt(PostgreSqlParser.Rollback_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#rollback_prepared_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRollback_prepared_stmt(PostgreSqlParser.Rollback_prepared_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#rollback_prepared_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRollback_prepared_stmt(PostgreSqlParser.Rollback_prepared_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#rollback_to_savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRollback_to_savepoint_stmt(PostgreSqlParser.Rollback_to_savepoint_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#rollback_to_savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRollback_to_savepoint_stmt(PostgreSqlParser.Rollback_to_savepoint_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_stmt(PostgreSqlParser.Savepoint_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_stmt(PostgreSqlParser.Savepoint_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#security_label_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSecurity_label_stmt(PostgreSqlParser.Security_label_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#security_label_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSecurity_label_stmt(PostgreSqlParser.Security_label_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSelect_stmt(PostgreSqlParser.Select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSelect_stmt(PostgreSqlParser.Select_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#select_into_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSelect_into_stmt(PostgreSqlParser.Select_into_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#select_into_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSelect_into_stmt(PostgreSqlParser.Select_into_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void enterWith_clause(PostgreSqlParser.With_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void exitWith_clause(PostgreSqlParser.With_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#with_expr}.
	 * @param ctx the parse tree
	 */
	void enterWith_expr(PostgreSqlParser.With_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#with_expr}.
	 * @param ctx the parse tree
	 */
	void exitWith_expr(PostgreSqlParser.With_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#set_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSet_stmt(PostgreSqlParser.Set_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#set_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSet_stmt(PostgreSqlParser.Set_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#set_constraints_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSet_constraints_stmt(PostgreSqlParser.Set_constraints_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#set_constraints_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSet_constraints_stmt(PostgreSqlParser.Set_constraints_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#set_role_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSet_role_stmt(PostgreSqlParser.Set_role_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#set_role_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSet_role_stmt(PostgreSqlParser.Set_role_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#set_session_authorization_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSet_session_authorization_stmt(PostgreSqlParser.Set_session_authorization_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#set_session_authorization_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSet_session_authorization_stmt(PostgreSqlParser.Set_session_authorization_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#transaction_mode}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_mode(PostgreSqlParser.Transaction_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#transaction_mode}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_mode(PostgreSqlParser.Transaction_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#transaction_mode_list}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_mode_list(PostgreSqlParser.Transaction_mode_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#transaction_mode_list}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_mode_list(PostgreSqlParser.Transaction_mode_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#set_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSet_transaction_stmt(PostgreSqlParser.Set_transaction_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#set_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSet_transaction_stmt(PostgreSqlParser.Set_transaction_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#show_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_stmt(PostgreSqlParser.Show_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#show_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_stmt(PostgreSqlParser.Show_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#truncate_stmt}.
	 * @param ctx the parse tree
	 */
	void enterTruncate_stmt(PostgreSqlParser.Truncate_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#truncate_stmt}.
	 * @param ctx the parse tree
	 */
	void exitTruncate_stmt(PostgreSqlParser.Truncate_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#unlisten_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUnlisten_stmt(PostgreSqlParser.Unlisten_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#unlisten_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUnlisten_stmt(PostgreSqlParser.Unlisten_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_stmt(PostgreSqlParser.Update_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_stmt(PostgreSqlParser.Update_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#vacuum_opt}.
	 * @param ctx the parse tree
	 */
	void enterVacuum_opt(PostgreSqlParser.Vacuum_optContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#vacuum_opt}.
	 * @param ctx the parse tree
	 */
	void exitVacuum_opt(PostgreSqlParser.Vacuum_optContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#vacuum_opt_list}.
	 * @param ctx the parse tree
	 */
	void enterVacuum_opt_list(PostgreSqlParser.Vacuum_opt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#vacuum_opt_list}.
	 * @param ctx the parse tree
	 */
	void exitVacuum_opt_list(PostgreSqlParser.Vacuum_opt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#vacuum_stmt}.
	 * @param ctx the parse tree
	 */
	void enterVacuum_stmt(PostgreSqlParser.Vacuum_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#vacuum_stmt}.
	 * @param ctx the parse tree
	 */
	void exitVacuum_stmt(PostgreSqlParser.Vacuum_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#values_stmt}.
	 * @param ctx the parse tree
	 */
	void enterValues_stmt(PostgreSqlParser.Values_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#values_stmt}.
	 * @param ctx the parse tree
	 */
	void exitValues_stmt(PostgreSqlParser.Values_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#selector_clause}.
	 * @param ctx the parse tree
	 */
	void enterSelector_clause(PostgreSqlParser.Selector_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#selector_clause}.
	 * @param ctx the parse tree
	 */
	void exitSelector_clause(PostgreSqlParser.Selector_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrom_clause(PostgreSqlParser.From_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrom_clause(PostgreSqlParser.From_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_clause(PostgreSqlParser.Where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_clause(PostgreSqlParser.Where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_clause(PostgreSqlParser.Group_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_clause(PostgreSqlParser.Group_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#grouping_elem}.
	 * @param ctx the parse tree
	 */
	void enterGrouping_elem(PostgreSqlParser.Grouping_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#grouping_elem}.
	 * @param ctx the parse tree
	 */
	void exitGrouping_elem(PostgreSqlParser.Grouping_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#grouping_elem_list}.
	 * @param ctx the parse tree
	 */
	void enterGrouping_elem_list(PostgreSqlParser.Grouping_elem_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#grouping_elem_list}.
	 * @param ctx the parse tree
	 */
	void exitGrouping_elem_list(PostgreSqlParser.Grouping_elem_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void enterHaving_clause(PostgreSqlParser.Having_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void exitHaving_clause(PostgreSqlParser.Having_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#column_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_list(PostgreSqlParser.Column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#column_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_list(PostgreSqlParser.Column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#explain_parameter}.
	 * @param ctx the parse tree
	 */
	void enterExplain_parameter(PostgreSqlParser.Explain_parameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#explain_parameter}.
	 * @param ctx the parse tree
	 */
	void exitExplain_parameter(PostgreSqlParser.Explain_parameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#frame}.
	 * @param ctx the parse tree
	 */
	void enterFrame(PostgreSqlParser.FrameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#frame}.
	 * @param ctx the parse tree
	 */
	void exitFrame(PostgreSqlParser.FrameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#frame_start}.
	 * @param ctx the parse tree
	 */
	void enterFrame_start(PostgreSqlParser.Frame_startContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#frame_start}.
	 * @param ctx the parse tree
	 */
	void exitFrame_start(PostgreSqlParser.Frame_startContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#frame_end}.
	 * @param ctx the parse tree
	 */
	void enterFrame_end(PostgreSqlParser.Frame_endContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#frame_end}.
	 * @param ctx the parse tree
	 */
	void exitFrame_end(PostgreSqlParser.Frame_endContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#frame_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrame_clause(PostgreSqlParser.Frame_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#frame_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrame_clause(PostgreSqlParser.Frame_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#window_definition}.
	 * @param ctx the parse tree
	 */
	void enterWindow_definition(PostgreSqlParser.Window_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#window_definition}.
	 * @param ctx the parse tree
	 */
	void exitWindow_definition(PostgreSqlParser.Window_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#window_clause}.
	 * @param ctx the parse tree
	 */
	void enterWindow_clause(PostgreSqlParser.Window_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#window_clause}.
	 * @param ctx the parse tree
	 */
	void exitWindow_clause(PostgreSqlParser.Window_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#combine_clause}.
	 * @param ctx the parse tree
	 */
	void enterCombine_clause(PostgreSqlParser.Combine_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#combine_clause}.
	 * @param ctx the parse tree
	 */
	void exitCombine_clause(PostgreSqlParser.Combine_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_clause(PostgreSqlParser.Order_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_clause(PostgreSqlParser.Order_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#order_by_item}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_item(PostgreSqlParser.Order_by_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#order_by_item}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_item(PostgreSqlParser.Order_by_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void enterLimit_clause(PostgreSqlParser.Limit_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void exitLimit_clause(PostgreSqlParser.Limit_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void enterOffset_clause(PostgreSqlParser.Offset_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void exitOffset_clause(PostgreSqlParser.Offset_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#fetch_clause}.
	 * @param ctx the parse tree
	 */
	void enterFetch_clause(PostgreSqlParser.Fetch_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#fetch_clause}.
	 * @param ctx the parse tree
	 */
	void exitFetch_clause(PostgreSqlParser.Fetch_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#for_clause}.
	 * @param ctx the parse tree
	 */
	void enterFor_clause(PostgreSqlParser.For_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#for_clause}.
	 * @param ctx the parse tree
	 */
	void exitFor_clause(PostgreSqlParser.For_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#updater_clause}.
	 * @param ctx the parse tree
	 */
	void enterUpdater_clause(PostgreSqlParser.Updater_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#updater_clause}.
	 * @param ctx the parse tree
	 */
	void exitUpdater_clause(PostgreSqlParser.Updater_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#updater_expr}.
	 * @param ctx the parse tree
	 */
	void enterUpdater_expr(PostgreSqlParser.Updater_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#updater_expr}.
	 * @param ctx the parse tree
	 */
	void exitUpdater_expr(PostgreSqlParser.Updater_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#returning_clause}.
	 * @param ctx the parse tree
	 */
	void enterReturning_clause(PostgreSqlParser.Returning_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#returning_clause}.
	 * @param ctx the parse tree
	 */
	void exitReturning_clause(PostgreSqlParser.Returning_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(PostgreSqlParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(PostgreSqlParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(PostgreSqlParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(PostgreSqlParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#parameterOrIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterParameterOrIntegerLiteral(PostgreSqlParser.ParameterOrIntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#parameterOrIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitParameterOrIntegerLiteral(PostgreSqlParser.ParameterOrIntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#parameterOrNumericLiteral}.
	 * @param ctx the parse tree
	 */
	void enterParameterOrNumericLiteral(PostgreSqlParser.ParameterOrNumericLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#parameterOrNumericLiteral}.
	 * @param ctx the parse tree
	 */
	void exitParameterOrNumericLiteral(PostgreSqlParser.ParameterOrNumericLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#bool_expr}.
	 * @param ctx the parse tree
	 */
	void enterBool_expr(PostgreSqlParser.Bool_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#bool_expr}.
	 * @param ctx the parse tree
	 */
	void exitBool_expr(PostgreSqlParser.Bool_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#case_expr}.
	 * @param ctx the parse tree
	 */
	void enterCase_expr(PostgreSqlParser.Case_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#case_expr}.
	 * @param ctx the parse tree
	 */
	void exitCase_expr(PostgreSqlParser.Case_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void enterExpr_list(PostgreSqlParser.Expr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void exitExpr_list(PostgreSqlParser.Expr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#expr_list_list}.
	 * @param ctx the parse tree
	 */
	void enterExpr_list_list(PostgreSqlParser.Expr_list_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#expr_list_list}.
	 * @param ctx the parse tree
	 */
	void exitExpr_list_list(PostgreSqlParser.Expr_list_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#func_sig_arg}.
	 * @param ctx the parse tree
	 */
	void enterFunc_sig_arg(PostgreSqlParser.Func_sig_argContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#func_sig_arg}.
	 * @param ctx the parse tree
	 */
	void exitFunc_sig_arg(PostgreSqlParser.Func_sig_argContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#func_sig_arg_list}.
	 * @param ctx the parse tree
	 */
	void enterFunc_sig_arg_list(PostgreSqlParser.Func_sig_arg_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#func_sig_arg_list}.
	 * @param ctx the parse tree
	 */
	void exitFunc_sig_arg_list(PostgreSqlParser.Func_sig_arg_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#func_sig}.
	 * @param ctx the parse tree
	 */
	void enterFunc_sig(PostgreSqlParser.Func_sigContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#func_sig}.
	 * @param ctx the parse tree
	 */
	void exitFunc_sig(PostgreSqlParser.Func_sigContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#func_sig_list}.
	 * @param ctx the parse tree
	 */
	void enterFunc_sig_list(PostgreSqlParser.Func_sig_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#func_sig_list}.
	 * @param ctx the parse tree
	 */
	void exitFunc_sig_list(PostgreSqlParser.Func_sig_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#type_name}.
	 * @param ctx the parse tree
	 */
	void enterType_name(PostgreSqlParser.Type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#type_name}.
	 * @param ctx the parse tree
	 */
	void exitType_name(PostgreSqlParser.Type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#timezone}.
	 * @param ctx the parse tree
	 */
	void enterTimezone(PostgreSqlParser.TimezoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#timezone}.
	 * @param ctx the parse tree
	 */
	void exitTimezone(PostgreSqlParser.TimezoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#oper}.
	 * @param ctx the parse tree
	 */
	void enterOper(PostgreSqlParser.OperContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#oper}.
	 * @param ctx the parse tree
	 */
	void exitOper(PostgreSqlParser.OperContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#aggregate}.
	 * @param ctx the parse tree
	 */
	void enterAggregate(PostgreSqlParser.AggregateContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#aggregate}.
	 * @param ctx the parse tree
	 */
	void exitAggregate(PostgreSqlParser.AggregateContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#name_}.
	 * @param ctx the parse tree
	 */
	void enterName_(PostgreSqlParser.Name_Context ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#name_}.
	 * @param ctx the parse tree
	 */
	void exitName_(PostgreSqlParser.Name_Context ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#name_list}.
	 * @param ctx the parse tree
	 */
	void enterName_list(PostgreSqlParser.Name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#name_list}.
	 * @param ctx the parse tree
	 */
	void exitName_list(PostgreSqlParser.Name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#identifier_list}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier_list(PostgreSqlParser.Identifier_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#identifier_list}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier_list(PostgreSqlParser.Identifier_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#option_expr}.
	 * @param ctx the parse tree
	 */
	void enterOption_expr(PostgreSqlParser.Option_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#option_expr}.
	 * @param ctx the parse tree
	 */
	void exitOption_expr(PostgreSqlParser.Option_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#option_list}.
	 * @param ctx the parse tree
	 */
	void enterOption_list(PostgreSqlParser.Option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#option_list}.
	 * @param ctx the parse tree
	 */
	void exitOption_list(PostgreSqlParser.Option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#table_name_}.
	 * @param ctx the parse tree
	 */
	void enterTable_name_(PostgreSqlParser.Table_name_Context ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#table_name_}.
	 * @param ctx the parse tree
	 */
	void exitTable_name_(PostgreSqlParser.Table_name_Context ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#data_type}.
	 * @param ctx the parse tree
	 */
	void enterData_type(PostgreSqlParser.Data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#data_type}.
	 * @param ctx the parse tree
	 */
	void exitData_type(PostgreSqlParser.Data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#data_type_list}.
	 * @param ctx the parse tree
	 */
	void enterData_type_list(PostgreSqlParser.Data_type_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#data_type_list}.
	 * @param ctx the parse tree
	 */
	void exitData_type_list(PostgreSqlParser.Data_type_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#index_method}.
	 * @param ctx the parse tree
	 */
	void enterIndex_method(PostgreSqlParser.Index_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#index_method}.
	 * @param ctx the parse tree
	 */
	void exitIndex_method(PostgreSqlParser.Index_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#func_name}.
	 * @param ctx the parse tree
	 */
	void enterFunc_name(PostgreSqlParser.Func_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#func_name}.
	 * @param ctx the parse tree
	 */
	void exitFunc_name(PostgreSqlParser.Func_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#func_call}.
	 * @param ctx the parse tree
	 */
	void enterFunc_call(PostgreSqlParser.Func_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#func_call}.
	 * @param ctx the parse tree
	 */
	void exitFunc_call(PostgreSqlParser.Func_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#array_cons_expr}.
	 * @param ctx the parse tree
	 */
	void enterArray_cons_expr(PostgreSqlParser.Array_cons_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#array_cons_expr}.
	 * @param ctx the parse tree
	 */
	void exitArray_cons_expr(PostgreSqlParser.Array_cons_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#from_item}.
	 * @param ctx the parse tree
	 */
	void enterFrom_item(PostgreSqlParser.From_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#from_item}.
	 * @param ctx the parse tree
	 */
	void exitFrom_item(PostgreSqlParser.From_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#with_column_alias}.
	 * @param ctx the parse tree
	 */
	void enterWith_column_alias(PostgreSqlParser.With_column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#with_column_alias}.
	 * @param ctx the parse tree
	 */
	void exitWith_column_alias(PostgreSqlParser.With_column_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#join_type}.
	 * @param ctx the parse tree
	 */
	void enterJoin_type(PostgreSqlParser.Join_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#join_type}.
	 * @param ctx the parse tree
	 */
	void exitJoin_type(PostgreSqlParser.Join_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void enterJoin_clause(PostgreSqlParser.Join_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void exitJoin_clause(PostgreSqlParser.Join_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(PostgreSqlParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(PostgreSqlParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#aggregate_signature}.
	 * @param ctx the parse tree
	 */
	void enterAggregate_signature(PostgreSqlParser.Aggregate_signatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#aggregate_signature}.
	 * @param ctx the parse tree
	 */
	void exitAggregate_signature(PostgreSqlParser.Aggregate_signatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void enterColumn_constraint(PostgreSqlParser.Column_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void exitColumn_constraint(PostgreSqlParser.Column_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#column_constraints}.
	 * @param ctx the parse tree
	 */
	void enterColumn_constraints(PostgreSqlParser.Column_constraintsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#column_constraints}.
	 * @param ctx the parse tree
	 */
	void exitColumn_constraints(PostgreSqlParser.Column_constraintsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#index_parameters}.
	 * @param ctx the parse tree
	 */
	void enterIndex_parameters(PostgreSqlParser.Index_parametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#index_parameters}.
	 * @param ctx the parse tree
	 */
	void exitIndex_parameters(PostgreSqlParser.Index_parametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#exclude_element}.
	 * @param ctx the parse tree
	 */
	void enterExclude_element(PostgreSqlParser.Exclude_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#exclude_element}.
	 * @param ctx the parse tree
	 */
	void exitExclude_element(PostgreSqlParser.Exclude_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void enterTable_constraint(PostgreSqlParser.Table_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void exitTable_constraint(PostgreSqlParser.Table_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#role_name}.
	 * @param ctx the parse tree
	 */
	void enterRole_name(PostgreSqlParser.Role_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#role_name}.
	 * @param ctx the parse tree
	 */
	void exitRole_name(PostgreSqlParser.Role_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#role_name_list}.
	 * @param ctx the parse tree
	 */
	void enterRole_name_list(PostgreSqlParser.Role_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#role_name_list}.
	 * @param ctx the parse tree
	 */
	void exitRole_name_list(PostgreSqlParser.Role_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#param_value}.
	 * @param ctx the parse tree
	 */
	void enterParam_value(PostgreSqlParser.Param_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#param_value}.
	 * @param ctx the parse tree
	 */
	void exitParam_value(PostgreSqlParser.Param_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#reserved_keyword}.
	 * @param ctx the parse tree
	 */
	void enterReserved_keyword(PostgreSqlParser.Reserved_keywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#reserved_keyword}.
	 * @param ctx the parse tree
	 */
	void exitReserved_keyword(PostgreSqlParser.Reserved_keywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#non_reserved_keyword}.
	 * @param ctx the parse tree
	 */
	void enterNon_reserved_keyword(PostgreSqlParser.Non_reserved_keywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#non_reserved_keyword}.
	 * @param ctx the parse tree
	 */
	void exitNon_reserved_keyword(PostgreSqlParser.Non_reserved_keywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(PostgreSqlParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(PostgreSqlParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#todo_fill_in}.
	 * @param ctx the parse tree
	 */
	void enterTodo_fill_in(PostgreSqlParser.Todo_fill_inContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#todo_fill_in}.
	 * @param ctx the parse tree
	 */
	void exitTodo_fill_in(PostgreSqlParser.Todo_fill_inContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#todo_implement}.
	 * @param ctx the parse tree
	 */
	void enterTodo_implement(PostgreSqlParser.Todo_implementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#todo_implement}.
	 * @param ctx the parse tree
	 */
	void exitTodo_implement(PostgreSqlParser.Todo_implementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#correlation_name}.
	 * @param ctx the parse tree
	 */
	void enterCorrelation_name(PostgreSqlParser.Correlation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#correlation_name}.
	 * @param ctx the parse tree
	 */
	void exitCorrelation_name(PostgreSqlParser.Correlation_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(PostgreSqlParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(PostgreSqlParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(PostgreSqlParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(PostgreSqlParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias(PostgreSqlParser.Column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias(PostgreSqlParser.Column_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#column_definition}.
	 * @param ctx the parse tree
	 */
	void enterColumn_definition(PostgreSqlParser.Column_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#column_definition}.
	 * @param ctx the parse tree
	 */
	void exitColumn_definition(PostgreSqlParser.Column_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSqlParser#window_name}.
	 * @param ctx the parse tree
	 */
	void enterWindow_name(PostgreSqlParser.Window_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSqlParser#window_name}.
	 * @param ctx the parse tree
	 */
	void exitWindow_name(PostgreSqlParser.Window_nameContext ctx);
}
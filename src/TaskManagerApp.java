import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.*;
import java.util.stream.Collectors;

/**
 * Enhanced Single-file Java OOP Task Manager App
 * - Copy to TaskManagerApp.java
 * - Compile: javac TaskManagerApp.java
 * - Run: java TaskManagerApp
 */

public class TaskManagerApp {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        manager.run();
    }
}

/* -----------------------------
   Enums for Task Categories and Priorities
   ----------------------------- */
enum Priority {
    LOW, MEDIUM, HIGH, URGENT
}

enum Category {
    WORK, PERSONAL, STUDY, SHOPPING, HEALTH, FINANCE, OTHER
}

/* -----------------------------
   Domain class: Task
   ----------------------------- */
class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int nextId = 1;

    private final int id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private boolean completed;
    private Priority priority;
    private Category category;
    private List<String> tags;
    private int estimatedHours;
    private LocalDateTime completedDate;

    public Task(String title, String description, LocalDate dueDate,
                Priority priority, Category category) {
        this.id = nextId++;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.completed = false;
        this.priority = priority;
        this.category = category;
        this.tags = new ArrayList<>();
        this.estimatedHours = 0;
        this.completedDate = null;
    }

    // Getter for nextId (package-private)
    static int getNextId() {
        return nextId;
    }

    // Setter for nextId (package-private)
    static void setNextId(int id) {
        nextId = id;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getLastModified() { return lastModified; }
    public boolean isCompleted() { return completed; }
    public Priority getPriority() { return priority; }
    public Category getCategory() { return category; }
    public List<String> getTags() { return tags; }
    public int getEstimatedHours() { return estimatedHours; }
    public LocalDateTime getCompletedDate() { return completedDate; }

    // Setters with modification tracking
    public void setTitle(String title) {
        this.title = title;
        updateLastModified();
    }

    public void setDescription(String description) {
        this.description = description;
        updateLastModified();
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        updateLastModified();
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
        updateLastModified();
    }

    public void setCategory(Category category) {
        this.category = category;
        updateLastModified();
    }

    public void setEstimatedHours(int hours) {
        this.estimatedHours = Math.max(0, hours);
        updateLastModified();
    }

    public void markComplete() {
        this.completed = true;
        this.completedDate = LocalDateTime.now();
        updateLastModified();
    }

    public void markIncomplete() {
        this.completed = false;
        this.completedDate = null;
        updateLastModified();
    }

    public void addTag(String tag) {
        if (!tags.contains(tag.trim().toLowerCase())) {
            tags.add(tag.trim().toLowerCase());
            updateLastModified();
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag.trim().toLowerCase());
        updateLastModified();
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag.trim().toLowerCase());
    }

    public boolean isOverdue() {
        return !completed && dueDate.isBefore(LocalDate.now());
    }

    public long daysUntilDue() {
        return LocalDate.now().until(dueDate).getDays();
    }

    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }

    @Override
    public String toString() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        String status = completed ? "✅ Completed" : (isOverdue() ? "⏰ OVERDUE" : "⏳ Pending");
        String priorityIcon = switch(priority) {
            case LOW -> "🟢";
            case MEDIUM -> "🟡";
            case HIGH -> "🟠";
            case URGENT -> "🔴";
        };

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n%s [%d] %s\n", priorityIcon, id, title));
        sb.append(String.format("   📝 %s\n", description));
        sb.append(String.format("   📅 Due: %s | 📍 %s | 🏷️ %s\n",
                dueDate.format(dateFmt), category, status));

        if (!tags.isEmpty()) {
            sb.append(String.format("   🏷️ Tags: %s\n", String.join(", ", tags)));
        }

        if (estimatedHours > 0) {
            sb.append(String.format("   ⏱️ Estimated: %d hours\n", estimatedHours));
        }

        if (completed && completedDate != null) {
            sb.append(String.format("   ✅ Completed on: %s at %s\n",
                    completedDate.format(dateFmt), completedDate.format(timeFmt)));
        } else if (daysUntilDue() >= 0) {
            sb.append(String.format("   📊 Days until due: %d\n", daysUntilDue()));
        }

        sb.append(String.format("   📌 Created: %s | 🔄 Modified: %s",
                createdDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                lastModified.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));

        return sb.toString();
    }
}

/* -----------------------------
   Task Statistics Class
   ----------------------------- */
class TaskStatistics {
    private final Map<Integer, Task> tasks;

    public TaskStatistics(Map<Integer, Task> tasks) {
        this.tasks = tasks;
    }

    public long getTotalTasks() {
        return tasks.size();
    }

    public long getCompletedTasks() {
        return tasks.values().stream().filter(Task::isCompleted).count();
    }

    public long getPendingTasks() {
        return tasks.values().stream().filter(t -> !t.isCompleted()).count();
    }

    public long getOverdueTasks() {
        return tasks.values().stream().filter(Task::isOverdue).count();
    }

    public Map<Category, Long> getTasksByCategory() {
        return tasks.values().stream()
                .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));
    }

    public Map<Priority, Long> getTasksByPriority() {
        return tasks.values().stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }

    public double getCompletionRate() {
        if (tasks.isEmpty()) return 0.0;
        return (double) getCompletedTasks() / tasks.size() * 100;
    }

    public Task getMostUrgentTask() {
        return tasks.values().stream()
                .filter(t -> !t.isCompleted())
                .min(Comparator.comparing(Task::getDueDate)
                        .thenComparing(t -> t.getPriority().ordinal(), Comparator.reverseOrder()))
                .orElse(null);
    }

    public void showStatistics() {
        System.out.println("\n📊 TASK STATISTICS 📊");
        System.out.println("======================");
        System.out.printf("Total Tasks: %d\n", getTotalTasks());
        System.out.printf("Completed: %d (%.1f%%)\n", getCompletedTasks(), getCompletionRate());
        System.out.printf("Pending: %d\n", getPendingTasks());
        System.out.printf("Overdue: %d\n", getOverdueTasks());

        System.out.println("\n📈 By Category:");
        getTasksByCategory().forEach((cat, count) ->
                System.out.printf("  %s: %d\n", cat, count));

        System.out.println("\n📈 By Priority:");
        getTasksByPriority().forEach((pri, count) ->
                System.out.printf("  %s: %d\n", pri, count));

        Task urgent = getMostUrgentTask();
        if (urgent != null) {
            System.out.printf("\n🚨 Most Urgent Task: %s (Due: %s)\n",
                    urgent.getTitle(), urgent.getDueDate());
        }
    }
}

/* -----------------------------
   Task Manager: business logic
   ----------------------------- */
class TaskManager {
    private final Map<Integer, Task> tasks = new LinkedHashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private final String DATA_FILE = "tasks.dat";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void run() {
        loadTasksFromFile();
        System.out.println("\n🚀 ENHANCED TASK MANAGER APP 🚀");

        boolean running = true;
        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": addTask(); break;
                    case "2": updateTask(); break;
                    case "3": deleteTask(); break;
                    case "4": markComplete(); break;
                    case "5": markIncomplete(); break;
                    case "6": listTasks(); break;
                    case "7": listPendingTasks(); break;
                    case "8": searchTasks(); break;
                    case "9": filterTasks(); break;
                    case "10": sortTasks(); break;
                    case "11": showTaskDetails(); break;
                    case "12": manageTags(); break;
                    case "13": showStatistics(); break;
                    case "14": exportToText(); break;
                    case "15": importFromText(); break;
                    case "16": showUpcomingTasks(); break;
                    case "0":
                        running = false;
                        saveTasksToFile();
                        break;
                    default:
                        System.out.println("❌ Invalid choice. Please try again.");
                        break;
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Please use yyyy-MM-dd.");
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format.");
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
        System.out.println("\n👋 Exiting Task Manager. Goodbye!");
    }

    private void showMenu() {
        System.out.println("\n📋 MAIN MENU");
        System.out.println("============");
        System.out.println("1)  ➕ Add New Task");
        System.out.println("2)  ✏️  Update Task");
        System.out.println("3)  🗑️  Delete Task");
        System.out.println("4)  ✅ Mark Complete");
        System.out.println("5)  🔄 Mark Incomplete");
        System.out.println("6)  📋 List All Tasks");
        System.out.println("7)  ⏳ List Pending Tasks");
        System.out.println("8)  🔍 Search Tasks");
        System.out.println("9)  🎯 Filter Tasks");
        System.out.println("10) 📊 Sort Tasks");
        System.out.println("11) 📄 Show Task Details");
        System.out.println("12) 🏷️  Manage Tags");
        System.out.println("13) 📈 Show Statistics");
        System.out.println("14) 💾 Export to Text File");
        System.out.println("15) 📥 Import from Text File");
        System.out.println("16) 📅 Show Upcoming Tasks");
        System.out.println("0)  🚪 Exit");
        System.out.print("\n👉 Choose option: ");
    }

    private void addTask() {
        System.out.println("\n➕ ADD NEW TASK");
        System.out.println("================");

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("❌ Title cannot be empty!");
            return;
        }

        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();

        LocalDate due = null;
        while (due == null) {
            System.out.print("Due date (yyyy-mm-dd): ");
            String dateStr = scanner.nextLine().trim();
            try {
                due = LocalDate.parse(dateStr, DATE_FORMATTER);
                if (due.isBefore(LocalDate.now())) {
                    System.out.println("⚠️  Warning: Due date is in the past!");
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Please use yyyy-mm-dd.");
            }
        }

        System.out.println("\nPriority Levels:");
        for (Priority p : Priority.values()) {
            System.out.printf("  %d. %s\n", p.ordinal() + 1, p);
        }
        Priority priority = getEnumInput(Priority.class, "priority");

        System.out.println("\nCategories:");
        for (Category c : Category.values()) {
            System.out.printf("  %d. %s\n", c.ordinal() + 1, c);
        }
        Category category = getEnumInput(Category.class, "category");

        System.out.print("Estimated hours (0 for none): ");
        int hours = Integer.parseInt(scanner.nextLine().trim());

        Task task = new Task(title, desc, due, priority, category);
        if (hours > 0) {
            task.setEstimatedHours(hours);
        }

        tasks.put(task.getId(), task);
        System.out.println("\n✅ Task added successfully!");
        System.out.println(task);
    }

    private <T extends Enum<T>> T getEnumInput(Class<T> enumClass, String type) {
        while (true) {
            System.out.printf("Choose %s (1-%d): ", type, enumClass.getEnumConstants().length);
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= enumClass.getEnumConstants().length) {
                    return enumClass.getEnumConstants()[choice - 1];
                }
            } catch (NumberFormatException e) {
                // Try parsing by name
                try {
                    return Enum.valueOf(enumClass, input.toUpperCase());
                } catch (IllegalArgumentException e2) {
                    System.out.printf("❌ Invalid %s. Please enter a number or name.\n", type);
                }
            }
        }
    }

    private void updateTask() {
        Task task = findTaskById();
        if (task == null) return;

        System.out.println("\n✏️ UPDATE TASK");
        System.out.println("==============");
        System.out.println("Current task:");
        System.out.println(task);
        System.out.println("\nEnter new values (leave empty to keep current):");

        System.out.printf("New title [%s]: ", task.getTitle());
        String t = scanner.nextLine().trim();
        if (!t.isEmpty()) task.setTitle(t);

        System.out.printf("New description [%s]: ", task.getDescription());
        String d = scanner.nextLine().trim();
        if (!d.isEmpty()) task.setDescription(d);

        System.out.printf("New due date [%s]: ", task.getDueDate());
        String dateStr = scanner.nextLine().trim();
        if (!dateStr.isEmpty()) {
            try {
                task.setDueDate(LocalDate.parse(dateStr, DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Keeping current date.");
            }
        }

        System.out.printf("New priority [%s] (LOW/MEDIUM/HIGH/URGENT): ", task.getPriority());
        String pri = scanner.nextLine().trim();
        if (!pri.isEmpty()) {
            try {
                task.setPriority(Priority.valueOf(pri.toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Invalid priority. Keeping current.");
            }
        }

        System.out.printf("New category [%s] (WORK/PERSONAL/STUDY/...): ", task.getCategory());
        String cat = scanner.nextLine().trim();
        if (!cat.isEmpty()) {
            try {
                task.setCategory(Category.valueOf(cat.toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Invalid category. Keeping current.");
            }
        }

        System.out.printf("New estimated hours [%d]: ", task.getEstimatedHours());
        String hoursStr = scanner.nextLine().trim();
        if (!hoursStr.isEmpty()) {
            try {
                task.setEstimatedHours(Integer.parseInt(hoursStr));
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number. Keeping current.");
            }
        }

        System.out.println("\n✅ Task updated successfully!");
        System.out.println(task);
    }

    private void deleteTask() {
        Task task = findTaskById();
        if (task == null) return;

        System.out.println("\n🗑️ DELETE TASK");
        System.out.println("==============");
        System.out.println(task);
        System.out.print("\n❓ Are you sure you want to delete this task? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("yes") || confirm.equals("y")) {
            tasks.remove(task.getId());
            System.out.println("✅ Task deleted successfully!");
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }

    private void markComplete() {
        Task task = findTaskById();
        if (task == null) return;

        task.markComplete();
        System.out.println("✅ Task marked as complete!");
        System.out.println(task);
    }

    private void markIncomplete() {
        Task task = findTaskById();
        if (task == null) return;

        task.markIncomplete();
        System.out.println("🔄 Task marked as incomplete!");
        System.out.println(task);
    }

    private void listTasks() {
        if (tasks.isEmpty()) {
            System.out.println("\n📭 No tasks available.");
            return;
        }

        System.out.println("\n📋 ALL TASKS");
        System.out.println("=============");
        tasks.values().forEach(System.out::println);
        System.out.printf("\n📊 Total: %d tasks\n", tasks.size());
    }

    private void listPendingTasks() {
        List<Task> pending = tasks.values().stream()
                .filter(t -> !t.isCompleted())
                .collect(Collectors.toList());

        if (pending.isEmpty()) {
            System.out.println("\n🎉 No pending tasks!");
            return;
        }

        System.out.println("\n⏳ PENDING TASKS");
        System.out.println("=================");
        pending.forEach(System.out::println);
        System.out.printf("\n📊 Total pending: %d tasks\n", pending.size());
    }

    private void searchTasks() {
        System.out.println("\n🔍 SEARCH TASKS");
        System.out.println("================");
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();

        List<Task> results = tasks.values().stream()
                .filter(t -> t.getTitle().toLowerCase().contains(keyword) ||
                        t.getDescription().toLowerCase().contains(keyword) ||
                        t.getTags().stream().anyMatch(tag -> tag.contains(keyword)))
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            System.out.println("❌ No tasks found matching: " + keyword);
            return;
        }

        System.out.printf("\n🔍 Search Results for '%s':\n", keyword);
        results.forEach(System.out::println);
        System.out.printf("\n📊 Found: %d tasks\n", results.size());
    }

    private void filterTasks() {
        System.out.println("\n🎯 FILTER TASKS");
        System.out.println("================");
        System.out.println("Filter by:");
        System.out.println("1. Category");
        System.out.println("2. Priority");
        System.out.println("3. Status (Completed/Pending)");
        System.out.println("4. Overdue");
        System.out.println("5. Tag");
        System.out.print("Choose filter: ");

        String choice = scanner.nextLine().trim();
        List<Task> filtered = new ArrayList<>();

        switch (choice) {
            case "1":
                System.out.print("Enter category: ");
                String cat = scanner.nextLine().trim().toUpperCase();
                try {
                    Category category = Category.valueOf(cat);
                    filtered = tasks.values().stream()
                            .filter(t -> t.getCategory() == category)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    System.out.println("❌ Invalid category.");
                    return;
                }
                break;

            case "2":
                System.out.print("Enter priority: ");
                String pri = scanner.nextLine().trim().toUpperCase();
                try {
                    Priority priority = Priority.valueOf(pri);
                    filtered = tasks.values().stream()
                            .filter(t -> t.getPriority() == priority)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    System.out.println("❌ Invalid priority.");
                    return;
                }
                break;

            case "3":
                System.out.print("Enter status (completed/pending): ");
                String status = scanner.nextLine().trim().toLowerCase();
                if (status.equals("completed")) {
                    filtered = tasks.values().stream()
                            .filter(Task::isCompleted)
                            .collect(Collectors.toList());
                } else if (status.equals("pending")) {
                    filtered = tasks.values().stream()
                            .filter(t -> !t.isCompleted())
                            .collect(Collectors.toList());
                } else {
                    System.out.println("❌ Invalid status.");
                    return;
                }
                break;

            case "4":
                filtered = tasks.values().stream()
                        .filter(Task::isOverdue)
                        .collect(Collectors.toList());
                break;

            case "5":
                System.out.print("Enter tag: ");
                String tag = scanner.nextLine().trim().toLowerCase();
                filtered = tasks.values().stream()
                        .filter(t -> t.hasTag(tag))
                        .collect(Collectors.toList());
                break;

            default:
                System.out.println("❌ Invalid choice.");
                return;
        }

        if (filtered.isEmpty()) {
            System.out.println("❌ No tasks match the filter.");
            return;
        }

        System.out.println("\n🎯 Filtered Tasks:");
        filtered.forEach(System.out::println);
        System.out.printf("\n📊 Found: %d tasks\n", filtered.size());
    }

    private void sortTasks() {
        System.out.println("\n📊 SORT TASKS");
        System.out.println("==============");
        System.out.println("Sort by:");
        System.out.println("1. Due Date (earliest first)");
        System.out.println("2. Priority (highest first)");
        System.out.println("3. Creation Date (newest first)");
        System.out.println("4. Title (alphabetical)");
        System.out.print("Choose sort option: ");

        String choice = scanner.nextLine().trim();
        List<Task> sorted = new ArrayList<>(tasks.values());

        switch (choice) {
            case "1":
                sorted.sort(Comparator.comparing(Task::getDueDate));
                System.out.println("\n📅 Sorted by Due Date:");
                break;
            case "2":
                sorted.sort(Comparator.comparing(Task::getPriority).reversed());
                System.out.println("\n🔴 Sorted by Priority:");
                break;
            case "3":
                sorted.sort(Comparator.comparing(Task::getCreatedDate).reversed());
                System.out.println("\n🆕 Sorted by Creation Date:");
                break;
            case "4":
                sorted.sort(Comparator.comparing(Task::getTitle));
                System.out.println("\n🔤 Sorted by Title:");
                break;
            default:
                System.out.println("❌ Invalid choice.");
                return;
        }

        sorted.forEach(System.out::println);
    }

    private void showTaskDetails() {
        Task task = findTaskById();
        if (task == null) return;

        System.out.println("\n📄 TASK DETAILS");
        System.out.println("================");
        System.out.println(task);
    }

    private void manageTags() {
        Task task = findTaskById();
        if (task == null) return;

        System.out.println("\n🏷️ MANAGE TAGS");
        System.out.println("==============");
        System.out.println("Current tags: " + task.getTags());
        System.out.println("\n1. Add tag");
        System.out.println("2. Remove tag");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.print("Enter tag to add: ");
                String tagToAdd = scanner.nextLine().trim();
                task.addTag(tagToAdd);
                System.out.println("✅ Tag added!");
                break;
            case "2":
                System.out.print("Enter tag to remove: ");
                String tagToRemove = scanner.nextLine().trim();
                task.removeTag(tagToRemove);
                System.out.println("✅ Tag removed!");
                break;
            default:
                System.out.println("❌ Invalid choice.");
                return;
        }

        System.out.println("Updated tags: " + task.getTags());
    }

    private void showStatistics() {
        TaskStatistics stats = new TaskStatistics(tasks);
        stats.showStatistics();
    }

    private void exportToText() {
        try (PrintWriter writer = new PrintWriter("tasks_export.txt")) {
            writer.println("TASK EXPORT - " + LocalDateTime.now());
            writer.println("=".repeat(50));

            for (Task task : tasks.values()) {
                writer.println(task);
                writer.println("-".repeat(50));
            }

            writer.printf("\nTotal tasks exported: %d\n", tasks.size());
            System.out.println("✅ Tasks exported to 'tasks_export.txt'");
        } catch (IOException e) {
            System.out.println("❌ Error exporting tasks: " + e.getMessage());
        }
    }

    private void importFromText() {
        System.out.println("⚠️  Note: This feature imports from a specific format.");
        System.out.println("For now, please use the app's data file.");
    }

    private void showUpcomingTasks() {
        System.out.print("\nEnter number of days to look ahead: ");
        int days = Integer.parseInt(scanner.nextLine().trim());

        LocalDate endDate = LocalDate.now().plusDays(days);

        List<Task> upcoming = tasks.values().stream()
                .filter(t -> !t.isCompleted())
                .filter(t -> !t.getDueDate().isBefore(LocalDate.now()))
                .filter(t -> !t.getDueDate().isAfter(endDate))
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());

        if (upcoming.isEmpty()) {
            System.out.printf("\n📭 No tasks due in the next %d days.\n", days);
            return;
        }

        System.out.printf("\n📅 UPCOMING TASKS (Next %d days)\n", days);
        System.out.println("==============================");
        upcoming.forEach(System.out::println);
        System.out.printf("\n📊 Total upcoming: %d tasks\n", upcoming.size());
    }

    private Task findTaskById() {
        if (tasks.isEmpty()) {
            System.out.println("📭 No tasks available.");
            return null;
        }

        System.out.print("\nEnter Task ID (or 'list' to see all): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("list")) {
            listTasks();
            System.out.print("\nEnter Task ID: ");
            input = scanner.nextLine().trim();
        }

        try {
            int id = Integer.parseInt(input);
            Task task = tasks.get(id);
            if (task == null) {
                System.out.println("❌ Task not found with ID: " + id);
                return null;
            }
            return task;
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID format.");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTasksFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Map<Integer, Task> loaded = (Map<Integer, Task>) ois.readObject();

            // Update nextId to avoid duplicate IDs
            int maxId = loaded.keySet().stream().max(Integer::compare).orElse(0);
            Task.setNextId(maxId + 1);

            tasks.clear();
            tasks.putAll(loaded);
            System.out.println("📂 Loaded " + tasks.size() + " tasks from file.");
        } catch (FileNotFoundException e) {
            System.out.println("ℹ️  No existing task file found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("❌ Error loading tasks: " + e.getMessage());
        }
    }

    private void saveTasksToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(tasks);
            System.out.println("💾 Saved " + tasks.size() + " tasks to file.");
        } catch (IOException e) {
            System.out.println("❌ Error saving tasks: " + e.getMessage());
        }
    }
}
